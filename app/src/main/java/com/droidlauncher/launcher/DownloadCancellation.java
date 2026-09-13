package com.droidlauncher.launcher;

import java.util.concurrent.atomic.AtomicBoolean;

/** Cooperative cancellation token for installation downloads. */
public final class DownloadCancellation {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void throwIfCancelled() throws java.io.IOException {
        if (isCancelled()) throw new java.io.IOException("Download cancelled");
    }
}
