package com.droidlauncher.launcher;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Runs installation work off the UI thread and persists recoverable progress. */
public final class InstallationController {
    public interface Listener {
        void onState(InstallationState state);
    }

    private final MinecraftDownloadOrchestrator orchestrator;
    private final InstallationStateStore stateStore;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public InstallationController(MinecraftDownloadOrchestrator orchestrator,
                                   InstallationStateStore stateStore) {
        if (orchestrator == null || stateStore == null) throw new IllegalArgumentException("dependencies are required");
        this.orchestrator = orchestrator;
        this.stateStore = stateStore;
    }

    public void cancel() { cancelled.set(true); }

    public void install(final String versionId, final List<DownloadTask> tasks, final Listener listener) {
        cancelled.set(false);
        final List<DownloadTask> work = tasks == null ? Collections.emptyList() : tasks;
        final int total = work.size();
        notifyState(listener, new InstallationState(versionId, InstallationState.Status.RUNNING,
                0, total, "", "", System.currentTimeMillis()));

        new Thread(() -> {
            final int[] completed = {0};
            try {
                orchestrator.install(work, progress -> {
                    if (cancelled.get()) throw new InstallationCancelledException();
                    completed[0] = Math.max(completed[0], progress.getCompletedTasks());
                    InstallationState state = new InstallationState(versionId,
                            InstallationState.Status.RUNNING,
                            completed[0], total,
                            progress.getTaskName(), "", System.currentTimeMillis());
                    stateStore.save(state);
                    notifyState(listener, state);
                });
                InstallationState done = new InstallationState(versionId,
                        InstallationState.Status.COMPLETED, total, total, "", "",
                        System.currentTimeMillis());
                stateStore.save(done);
                notifyState(listener, done);
            } catch (InstallationCancelledException e) {
                InstallationState cancelledState = new InstallationState(versionId,
                        InstallationState.Status.CANCELLED, completed[0], total, "",
                        "Installation cancelled", System.currentTimeMillis());
                stateStore.save(cancelledState);
                notifyState(listener, cancelledState);
            } catch (Exception e) {
                String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                InstallationState failed = new InstallationState(versionId,
                        InstallationState.Status.FAILED, completed[0], total, "", message,
                        System.currentTimeMillis());
                stateStore.save(failed);
                notifyState(listener, failed);
            }
        }, "droid-installation").start();
    }

    private void notifyState(Listener listener, InstallationState state) {
        if (listener != null) listener.onState(state);
    }

    /** Unchecked so cancellation can safely abort a progress callback. */
    private static final class InstallationCancelledException extends RuntimeException {
        InstallationCancelledException() { super("cancelled"); }
    }
}
