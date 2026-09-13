package com.droidlauncher.launcher;

/** Lifecycle states for one Minecraft launch attempt. */
public enum LaunchState {
    IDLE,
    PREFLIGHT,
    COMMAND_READY,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    FAILED
}
