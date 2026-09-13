package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/** Downloads and verifies the artifacts required by a Minecraft installation. */
public final class MinecraftDownloadOrchestrator {
    private final DownloadManager downloadManager;

    public MinecraftDownloadOrchestrator() {
        this(new DownloadManager());
    }

    public MinecraftDownloadOrchestrator(DownloadManager downloadManager) {
        if (downloadManager == null) throw new IllegalArgumentException("downloadManager is required");
        this.downloadManager = downloadManager;
    }

    /** Installs tasks in supplied order and returns a verified result summary. */
    public InstallResult install(List<DownloadTask> tasks,
                                 DownloadProgressListener listener,
                                 DownloadCancellation cancellation) throws IOException {
        List<DownloadTask> work = tasks == null ? Collections.emptyList() : tasks;
        long verifiedBytes = 0L;

        for (int i = 0; i < work.size(); i++) {
            if (cancellation != null) cancellation.throwIfCancelled();
            DownloadTask task = work.get(i);
            if (task == null) throw new IOException("Download task " + i + " is null");

            File destination = task.getDestination();
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Cannot create download directory: " + parent);
            }

            if (destination.isFile() && Sha1Verifier.verify(destination, task.getSha1())) {
                verifiedBytes += destination.length();
                notify(listener, new DownloadProgress(task.getName(), destination.length(),
                        destination.length(), i + 1, work.size()));
                continue;
            }

            if (destination.exists() && !destination.delete()) {
                throw new IOException("Cannot replace invalid artifact: " + destination);
            }

            notify(listener, new DownloadProgress(task.getName(), 0L, 0L,
                    i + 1, work.size()));
            downloadManager.download(task.getUrl(), destination);
            if (cancellation != null) cancellation.throwIfCancelled();

            if (!Sha1Verifier.verify(destination, task.getSha1())) {
                if (!destination.delete() && destination.exists()) {
                    throw new IOException("Checksum failed and invalid artifact could not be removed: " + destination);
                }
                throw new IOException("SHA-1 verification failed for " + task.getName());
            }

            verifiedBytes += destination.length();
            notify(listener, new DownloadProgress(task.getName(), destination.length(),
                    destination.length(), i + 1, work.size()));
        }

        if (cancellation != null) cancellation.throwIfCancelled();
        return new InstallResult(work.size(), verifiedBytes);
    }

    /** Backward-compatible install entry point without cancellation. */
    public void install(List<DownloadTask> tasks, DownloadProgressListener listener) throws IOException {
        install(tasks, listener, null);
    }

    private void notify(DownloadProgressListener listener, DownloadProgress progress) {
        if (listener != null) listener.onProgress(progress);
    }
}
