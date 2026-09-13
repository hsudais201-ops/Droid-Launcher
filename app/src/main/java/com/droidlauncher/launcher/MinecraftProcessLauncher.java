package com.droidlauncher.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Starts the Java process without a shell and captures both output streams. */
public final class MinecraftProcessLauncher {
    private final ExecutorService ioExecutor = Executors.newCachedThreadPool();

    public Process launch(MinecraftCommand command) throws IOException {
        if (command == null) throw new IllegalArgumentException("command is required");
        ProcessBuilder builder = new ProcessBuilder(command.toProcessBuilderCommand());
        builder.directory(command.getWorkingDirectory());
        builder.redirectErrorStream(false);
        Process process = builder.start();
        consumeAsync(process.getInputStream(), "stdout");
        consumeAsync(process.getErrorStream(), "stderr");
        return process;
    }

    private void consumeAsync(InputStream stream, String channel) {
        ioExecutor.execute(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    android.util.Log.i("DroidLauncher", "[Minecraft " + channel + "] " + line);
                }
            } catch (IOException e) {
                android.util.Log.w("DroidLauncher", "Minecraft " + channel + " stream closed", e);
            }
        });
    }

    public static List<String> combineJvmAndGameArguments(List<String> jvmArguments,
                                                           String classpath,
                                                           String mainClass,
                                                           List<String> gameArguments) {
        if (classpath == null || classpath.isEmpty()) {
            throw new IllegalArgumentException("classpath is required");
        }
        if (mainClass == null || mainClass.isEmpty()) {
            throw new IllegalArgumentException("mainClass is required");
        }
        List<String> command = new ArrayList<>();
        if (jvmArguments != null) command.addAll(jvmArguments);
        command.add("-cp");
        command.add(classpath);
        command.add(mainClass);
        if (gameArguments != null) command.addAll(gameArguments);
        return command;
    }

    public void shutdown() {
        ioExecutor.shutdownNow();
    }
}
