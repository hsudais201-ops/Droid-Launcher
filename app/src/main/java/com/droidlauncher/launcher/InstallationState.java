package com.droidlauncher.launcher;

/** Persistent-friendly state for a version installation. */
public final class InstallationState {
    public enum Status { IDLE, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED }

    private final String versionId;
    private final Status status;
    private final int completedTasks;
    private final int totalTasks;
    private final String currentTask;
    private final String error;
    private final long updatedAt;

    public InstallationState(String versionId, Status status, int completedTasks,
                             int totalTasks, String currentTask, String error, long updatedAt) {
        this.versionId = versionId == null ? "" : versionId;
        this.status = status == null ? Status.IDLE : status;
        this.completedTasks = Math.max(0, completedTasks);
        this.totalTasks = Math.max(0, totalTasks);
        this.currentTask = currentTask == null ? "" : currentTask;
        this.error = error == null ? "" : error;
        this.updatedAt = updatedAt;
    }

    public String getVersionId() { return versionId; }
    public Status getStatus() { return status; }
    public int getCompletedTasks() { return completedTasks; }
    public int getTotalTasks() { return totalTasks; }
    public String getCurrentTask() { return currentTask; }
    public String getError() { return error; }
    public long getUpdatedAt() { return updatedAt; }

    public boolean isFinished() {
        return status == Status.COMPLETED || status == Status.FAILED || status == Status.CANCELLED;
    }
}
