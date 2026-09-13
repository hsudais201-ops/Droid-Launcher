# Step 181 — Verified Download Execution

Droid Launcher now has a cancellable installation execution layer on top of the Step 180 installation plan.

## Added

- `DownloadCancellation` provides cooperative cancellation between verified download tasks.
- `InstallResult` reports task count and verified byte count.
- `MinecraftDownloadOrchestrator.install(..., cancellation)` executes the complete plan in order.
- Existing valid SHA-1 files are reused.
- Invalid or incomplete destination files are replaced.
- Every newly downloaded artifact is SHA-1 verified before the task is accepted.
- Cancellation is checked before each task, after each network download, and before success is returned.
- The existing two-argument `install` API remains available.

## Integrity

An installation is not reported successful unless every planned artifact is present and matches its required SHA-1 checksum.

## Next

Step 182 should persist installation state and expose installation/download progress to the Droid Launcher UI, including recovery after an interrupted download session.
