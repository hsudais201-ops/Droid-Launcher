package com.droidlauncher.launcher;

import java.util.List;

/**
 * Reconciles persisted installation state with the current artifact plan and resumes only when needed.
 */
public final class InstallationResumeService {
    public interface Listener {
        void onState(InstallationState state);
    }

    private final InstallationStateStore stateStore;
    private final InstallationController controller;

    public InstallationResumeService(InstallationStateStore stateStore,
                                     InstallationController controller) {
        if (stateStore == null || controller == null) {
            throw new IllegalArgumentException("dependencies are required");
        }
        this.stateStore = stateStore;
        this.controller = controller;
    }

    public InstallationState restore() {
        return stateStore.load();
    }

    /** Resumes a previously running/failed installation using the supplied current plan. */
    public boolean resumeIfNeeded(List<DownloadTask> tasks, Listener listener) {
        InstallationState state = stateStore.load();
        if (state.getVersionId().isEmpty()) return false;
        if (state.getStatus() != InstallationState.Status.RUNNING
                && state.getStatus() != InstallationState.Status.FAILED
                && state.getStatus() != InstallationState.Status.CANCELLED) {
            return false;
        }
        controller.install(state.getVersionId(), tasks,
                restored -> {
                    stateStore.save(restored);
                    if (listener != null) listener.onState(restored);
                });
        return true;
    }
}
