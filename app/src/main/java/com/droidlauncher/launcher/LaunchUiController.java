package com.droidlauncher.launcher;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.droidlauncher.runtime.JavaRuntime;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Bridges the real launch manager/process monitor onto Android UI safely. */
public final class LaunchUiController implements LaunchObserver {
    public interface LaunchCallback {
        void onProcessStarted(Process process);
        void onProcessExited(int exitCode);
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final TextView statusView;
    private final TextView detailView;
    private final MinecraftLaunchManager launchManager;
    private final MinecraftProcessMonitor processMonitor;

    public LaunchUiController(TextView statusView, TextView detailView) {
        this(statusView, detailView, new MinecraftLaunchManager(), new MinecraftProcessMonitor());
    }

    public LaunchUiController(TextView statusView,
                              TextView detailView,
                              MinecraftLaunchManager launchManager,
                              MinecraftProcessMonitor processMonitor) {
        if (statusView == null || detailView == null || launchManager == null || processMonitor == null) {
            throw new IllegalArgumentException("Launch UI dependencies are required");
        }
        this.statusView = statusView;
        this.detailView = detailView;
        this.launchManager = launchManager;
        this.processMonitor = processMonitor;
        LauncherBackgroundController.install((android.app.Activity) statusView.getContext());
    }

    public void launch(JavaRuntime runtime,
                       File gameDirectory,
                       File nativesDirectory,
                       String classpath,
                       String mainClass,
                       List<String> jvmArguments,
                       List<String> gameArguments,
                       Map<String, String> environment) {
        launch(runtime, gameDirectory, nativesDirectory, classpath, mainClass,
                jvmArguments, gameArguments, environment, null);
    }

    public void launch(JavaRuntime runtime,
                       File gameDirectory,
                       File nativesDirectory,
                       String classpath,
                       String mainClass,
                       List<String> jvmArguments,
                       List<String> gameArguments,
                       Map<String, String> environment,
                       LaunchCallback callback) {
        onLaunchUpdate(LaunchObservation.state(LaunchState.PREFLIGHT, "Preparing Minecraft launch..."));
        Thread thread = new Thread(() -> {
            try {
                Process process = launchManager.launch(runtime, gameDirectory, nativesDirectory,
                        classpath, mainClass,
                        jvmArguments == null ? Collections.emptyList() : jvmArguments,
                        gameArguments == null ? Collections.emptyList() : gameArguments,
                        environment == null ? Collections.emptyMap() : environment);
                onLaunchUpdate(LaunchObservation.running(process));
                if (callback != null) {
                    mainHandler.post(() -> callback.onProcessStarted(process));
                }
                processMonitor.monitor(process, new LaunchObserver() {
                    @Override
                    public void onLaunchUpdate(LaunchObservation observation) {
                        LaunchUiController.this.onLaunchUpdate(observation);
                        if (callback != null && observation != null
                                && observation.getState() == LaunchState.STOPPED) {
                            mainHandler.post(() -> callback.onProcessExited(observation.getExitCode()));
                        }
                    }
                });
            } catch (IOException | RuntimeException error) {
                String message = error.getMessage() == null ? "Minecraft launch failed" : error.getMessage();
                onLaunchUpdate(LaunchObservation.state(LaunchState.FAILED, message));
            }
        }, "droid-launcher-launch");
        thread.start();
    }

    public void stop() {
        launchManager.stop();
        onLaunchUpdate(LaunchObservation.state(LaunchState.STOPPED, "Minecraft stopped"));
    }

    public LaunchState getState() {
        return launchManager.getState();
    }

    @Override
    public void onLaunchUpdate(LaunchObservation observation) {
        if (observation == null) return;
        mainHandler.post(() -> {
            statusView.setText(observation.getState().name());
            String message = observation.getMessage();
            if (message == null || message.isEmpty()) {
                message = observation.getCategory() == LaunchDiagnostics.Category.NONE
                        ? "Launcher ready"
                        : LaunchDiagnostics.hint(observation.getCategory());
            } else if (observation.getState() == LaunchState.FAILED) {
                String hint = LaunchDiagnostics.hint(observation.getCategory());
                if (hint != null && !hint.isEmpty()) message += "\n" + hint;
            }
            detailView.setText(message);
        });
    }

    public void shutdown() {
        processMonitor.shutdown();
        launchManager.shutdown();
    }
}
