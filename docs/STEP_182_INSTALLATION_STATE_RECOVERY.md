# Step 182 — Installation State & Recovery

Droid Launcher now has a persistent installation-state layer.

## Added

- `InstallationState` — immutable status/progress model.
- `InstallationStateStore` — persists the latest installation state with Android `SharedPreferences`.
- `InstallationController` — executes installation work on a background thread, reports state changes, supports cancellation, and records failures.

## Recovery behavior

Downloads that already passed SHA-1 validation continue to be reused by the existing download orchestrator. After an app/process interruption, the stored state identifies the version, task counts, status, and last task/error so the UI can recover its visible installation status.

## Important limitation

This step does not yet resume a partially running controller instance automatically after process death. Actual HTTP byte-level resume remains provided by `DownloadManager`; the next integration step will connect persisted state to automatic UI restoration and installation re-entry.
