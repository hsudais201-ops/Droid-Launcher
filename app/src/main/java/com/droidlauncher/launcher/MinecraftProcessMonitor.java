package com.droidlauncher.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Monitors a launched Minecraft process and reports lifecycle/diagnostic events. */
public final class MinecraftProcessMonitor {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean closed;

    public void monitor(Process process, LaunchObserver observer) {
        if (process == null) throw new IllegalArgumentException("process is required");
        if (observer == null) throw new IllegalArgumentException("observer is required");
        if (closed) throw new IllegalStateException("monitor is closed");

        observer.onLaunchUpdate(LaunchObservation.running(process));
        executor.execute(() -> pump(process.getInputStream(), observer, "stdout"));
        executor.execute(() -> pump(process.getErrorStream(), observer, "stderr"));
        executor.execute(() -> awaitExit(process, observer));
    }

    private void pump(InputStream stream, LaunchObserver observer, String channel) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while (!closed && (line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    observer.onLaunchUpdate(LaunchObservation.state(
                            LaunchState.RUNNING, "[" + channel + "] " + line));
                }
            }
        } catch (IOException e) {
            if (!closed) {
                observer.onLaunchUpdate(LaunchObservation.state(
                        LaunchState.FAILED, channel + " stream error: " + e.getMessage()));
            }
        }
    }

    private void awaitExit(Process process, LaunchObserver observer) {
        try {
            int exitCode = process.waitFor();
            LaunchState state = exitCode == 0 ? LaunchState.STOPPED : LaunchState.FAILED;
            observer.onLaunchUpdate(LaunchObservation.exited(exitCode,
                    "Minecraft process exited with code " + exitCode));
            if (state == LaunchState.FAILED) {
                observer.onLaunchUpdate(LaunchObservation.state(state,
                        "Minecraft terminated unexpectedly (exit " + exitCode + ")"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (!closed) observer.onLaunchUpdate(
                    LaunchObservation.state(LaunchState.FAILED, "Process monitor interrupted"));
        }
    }

    public boolean awaitExit(Process process, long timeout, TimeUnit unit) throws InterruptedException {
        if (process == null) throw new IllegalArgumentException("process is required");
        if (unit == null) throw new IllegalArgumentException("time unit is required");
        return process.waitFor(timeout, unit);
    }

    public void shutdown() {
        closed = true;
        executor.shutdownNow();
    }
}
