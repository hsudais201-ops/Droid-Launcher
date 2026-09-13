package com.droidlauncher.launcher;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

/** Bridges background process observations onto Android UI safely. */
public final class LaunchUiController implements LaunchObserver {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final TextView statusView;
    private final TextView detailView;

    public LaunchUiController(TextView statusView, TextView detailView) {
        if (statusView == null || detailView == null) {
            throw new IllegalArgumentException("UI views are required");
        }
        this.statusView = statusView;
        this.detailView = detailView;
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
                        : observation.getCategory().name();
            }
            detailView.setText(message);
        });
    }
}
