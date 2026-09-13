package com.droidlauncher.launcher;

/** Receives progress updates while an installation downloads its required files. */
public interface DownloadProgressListener {
    void onProgress(DownloadProgress progress);
}
