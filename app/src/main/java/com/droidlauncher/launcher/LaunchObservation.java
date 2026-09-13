package com.droidlauncher.launcher;

/** Immutable snapshot of a launch attempt for UI/diagnostics consumers. */
public final class LaunchObservation {
    private final LaunchState state;
    private final long pid;
    private final int exitCode;
    private final String message;
    private final LaunchDiagnostics.Category category;

    private LaunchObservation(LaunchState state, long pid, int exitCode, String message,
                              LaunchDiagnostics.Category category) {
        this.state = state;
        this.pid = pid;
        this.exitCode = exitCode;
        this.message = message == null ? "" : message;
        this.category = category == null ? LaunchDiagnostics.Category.NONE : category;
    }

    public static LaunchObservation state(LaunchState state, String message) {
        return new LaunchObservation(state, -1L, Integer.MIN_VALUE, message,
                LaunchDiagnostics.classify(message));
    }

    public static LaunchObservation running(Process process) {
        // Process.pid() is not available on all Android Java APIs/desugaring configurations.
        // Keep the diagnostic PID unknown rather than using a misleading surrogate value.
        return new LaunchObservation(LaunchState.RUNNING, -1L, Integer.MIN_VALUE, "",
                LaunchDiagnostics.Category.NONE);
    }

    public static LaunchObservation exited(int exitCode, String message) {
        return new LaunchObservation(LaunchState.STOPPED, -1L, exitCode, message,
                LaunchDiagnostics.classify(message));
    }

    public LaunchState getState() { return state; }
    public long getPid() { return pid; }
    public int getExitCode() { return exitCode; }
    public String getMessage() { return message; }
    public LaunchDiagnostics.Category getCategory() { return category; }
}
