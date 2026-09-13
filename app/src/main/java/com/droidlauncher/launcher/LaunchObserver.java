package com.droidlauncher.launcher;

/** Receives launch lifecycle updates from the unified process manager. */
public interface LaunchObserver {
    void onLaunchUpdate(LaunchObservation observation);
}
