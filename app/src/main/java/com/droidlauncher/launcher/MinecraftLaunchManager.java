package com.droidlauncher.launcher;

import com.droidlauncher.runtime.JavaRuntime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Coordinates preflight, command construction, process startup, and shutdown. */
public final class MinecraftLaunchManager {
    private final MinecraftProcessLauncher processLauncher;
    private final LaunchPreflight preflight;
    private volatile LaunchState state = LaunchState.IDLE;
    private volatile Process process;
    private volatile String lastError = "";

    public MinecraftLaunchManager() {
        this(new MinecraftProcessLauncher(), new LaunchPreflight());
    }

    public MinecraftLaunchManager(MinecraftProcessLauncher processLauncher, LaunchPreflight preflight) {
        if (processLauncher == null || preflight == null) {
            throw new IllegalArgumentException("launch dependencies are required");
        }
        this.processLauncher = processLauncher;
        this.preflight = preflight;
    }

    public synchronized Process launch(JavaRuntime runtime,
                                       File gameDirectory,
                                       File nativesDirectory,
                                       String classpath,
                                       String mainClass,
                                       List<String> jvmArguments,
                                       List<String> gameArguments,
                                       Map<String, String> environment) throws IOException {
        if (process != null && process.isAlive()) {
            throw new IllegalStateException("Minecraft is already running");
        }

        state = LaunchState.PREFLIGHT;
        lastError = "";
        LaunchPreflight.Result check = preflight.validate(runtime, gameDirectory,
                nativesDirectory, classpath, mainClass);
        if (!check.isValid()) {
            state = LaunchState.FAILED;
            lastError = check.getMessage();
            throw new IOException(check.getMessage());
        }

        List<String> commandArguments = MinecraftProcessLauncher.combineJvmAndGameArguments(
                jvmArguments == null ? Collections.emptyList() : jvmArguments,
                classpath, mainClass,
                gameArguments == null ? Collections.emptyList() : gameArguments);

        // Native loading is explicit through -Djava.library.path; no shell is used.
        List<String> rebuilt = new ArrayList<>(commandArguments.size() + 1);
        rebuilt.add("-Djava.library.path=" + nativesDirectory.getAbsolutePath());
        rebuilt.addAll(commandArguments);

        MinecraftCommand command;
        try {
            command = new MinecraftCommand(runtime.getJavaExecutable(), gameDirectory, rebuilt);
        } catch (RuntimeException e) {
            state = LaunchState.FAILED;
            lastError = e.getMessage() == null ? "Invalid Minecraft command" : e.getMessage();
            throw new IOException(lastError, e);
        }

        state = LaunchState.COMMAND_READY;
        try {
            ProcessBuilder builder = new ProcessBuilder(command.toProcessBuilderCommand());
            builder.directory(gameDirectory);
            builder.redirectErrorStream(false);
            if (environment != null) builder.environment().putAll(environment);
            state = LaunchState.STARTING;
            process = builder.start();
            state = LaunchState.RUNNING;
            return process;
        } catch (IOException e) {
            process = null;
            state = LaunchState.FAILED;
            lastError = e.getMessage() == null ? "Minecraft process failed to start" : e.getMessage();
            throw e;
        }
    }

    public synchronized void stop() {
        Process current = process;
        if (current == null) {
            state = LaunchState.STOPPED;
            return;
        }
        if (current.isAlive()) {
            state = LaunchState.STOPPING;
            current.destroy();
            try {
                if (!current.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    current.destroyForcibly();
                    current.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                current.destroyForcibly();
            }
        }
        process = null;
        state = LaunchState.STOPPED;
    }

    public LaunchState getState() { return state; }
    public Process getProcess() { return process; }
    public String getLastError() { return lastError; }

    public void shutdown() {
        stop();
        processLauncher.shutdown();
    }
}
