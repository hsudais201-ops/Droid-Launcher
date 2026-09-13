package com.droidlauncher.launcher;

/** Result summary for a verified installation pass. */
public final class InstallResult {
    private final int taskCount;
    private final long verifiedBytes;

    public InstallResult(int taskCount, long verifiedBytes) {
        this.taskCount = taskCount;
        this.verifiedBytes = verifiedBytes;
    }

    public int getTaskCount() { return taskCount; }
    public long getVerifiedBytes() { return verifiedBytes; }
}
