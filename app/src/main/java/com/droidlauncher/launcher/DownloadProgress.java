package com.droidlauncher.launcher;

/** Immutable progress snapshot for one download/install operation. */
public final class DownloadProgress {
    private final String name;
    private final long completedBytes;
    private final long totalBytes;
    private final int itemIndex;
    private final int itemCount;

    public DownloadProgress(String name, long completedBytes, long totalBytes,
                            int itemIndex, int itemCount) {
        this.name = name == null ? "" : name;
        this.completedBytes = Math.max(0L, completedBytes);
        this.totalBytes = Math.max(0L, totalBytes);
        this.itemIndex = Math.max(0, itemIndex);
        this.itemCount = Math.max(0, itemCount);
    }

    public String getName() { return name; }
    public long getCompletedBytes() { return completedBytes; }
    public long getTotalBytes() { return totalBytes; }
    public int getItemIndex() { return itemIndex; }
    public int getItemCount() { return itemCount; }

    /** Compatibility aliases used by the installation controller/UI. */
    public int getCompletedTasks() { return itemIndex; }
    public int getTotalTasks() { return itemCount; }
    public String getTaskName() { return name; }

    public int getPercent() {
        if (totalBytes <= 0L) return 0;
        return (int) Math.min(100L, (completedBytes * 100L) / totalBytes);
    }
}
