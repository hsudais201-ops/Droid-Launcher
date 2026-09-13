package com.droidlauncher.launcher;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores the latest installation state so an interrupted install can be recovered after process death. */
public final class InstallationStateStore {
    private static final String PREFS = "droid_launcher_installation";
    private final SharedPreferences prefs;

    public InstallationStateStore(Context context) {
        if (context == null) throw new IllegalArgumentException("context is required");
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(InstallationState state) {
        if (state == null) throw new IllegalArgumentException("state is required");
        prefs.edit()
                .putString("versionId", state.getVersionId())
                .putString("status", state.getStatus().name())
                .putInt("completedTasks", state.getCompletedTasks())
                .putInt("totalTasks", state.getTotalTasks())
                .putString("currentTask", state.getCurrentTask())
                .putString("error", state.getError())
                .putLong("updatedAt", state.getUpdatedAt())
                .apply();
    }

    public InstallationState load() {
        String versionId = prefs.getString("versionId", "");
        if (versionId.isEmpty()) return new InstallationState("", InstallationState.Status.IDLE,
                0, 0, "", "", 0L);
        InstallationState.Status status;
        try {
            status = InstallationState.Status.valueOf(
                    prefs.getString("status", InstallationState.Status.IDLE.name()));
        } catch (Exception ignored) {
            status = InstallationState.Status.IDLE;
        }
        return new InstallationState(
                versionId,
                status,
                prefs.getInt("completedTasks", 0),
                prefs.getInt("totalTasks", 0),
                prefs.getString("currentTask", ""),
                prefs.getString("error", ""),
                prefs.getLong("updatedAt", 0L));
    }

    public void clear() { prefs.edit().clear().apply(); }
}
