# Step 177 — Minecraft Download Orchestrator

Step 177 adds the installation download layer between version metadata and the local runtime.

## Added

- `DownloadTask`: immutable URL, destination and mandatory SHA-1 metadata.
- `DownloadProgress` and `DownloadProgressListener`: progress snapshots for launcher UI.
- `MinecraftDownloadOrchestrator`: sequentially installs required artifacts using the existing resumable `DownloadManager`.

## Behavior

1. Reuses an existing artifact only when its SHA-1 matches.
2. Deletes an invalid cached artifact before a fresh download.
3. Creates destination directories as required.
4. Uses the existing resumable downloader for network transfer.
5. Verifies SHA-1 after every download.
6. Removes a failed artifact instead of leaving a corrupt file in the cache.
7. Reports per-file progress before and after installation.

## Scope

The orchestrator is deliberately generic. Version JSON parsing decides which client, library and asset objects become `DownloadTask` instances. This keeps networking, metadata parsing, and launch orchestration separate.

## Limitation

`DownloadManager` currently reports completion rather than byte-by-byte transfer progress, so progress callbacks are item-level snapshots. A future streaming progress implementation can add byte-level updates without changing the installation API.

## Verification status

Source changes are committed, but the GitHub Actions APK build and a real Minecraft installation/boot have not been verified by an executed workflow/device test.
