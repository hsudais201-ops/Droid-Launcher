# Step 178 — Version Download Planning

Step 178 converts parsed Minecraft version metadata into concrete local download tasks.

## Added

- `VersionDownloadPlanner`
- Client JAR destination under `versions/<version>/<version>.jar`
- Asset-index destination under `assets/indexes/<asset-index>.json`
- Required SHA-1 checks before accepting a task
- Safe version-directory routing through `VersionInstallation`

## Flow

`VersionMetadata` → `VersionDownloadPlanner` → `DownloadTask` → `MinecraftDownloadOrchestrator` → verified local files

## Current limitation

The current `VersionMetadata` model does not yet expose the full `libraries` and individual asset-object metadata from Mojang manifests. Those downloads remain the next expansion of the installation planner.

The planner also does not itself perform network I/O; the existing download orchestrator remains responsible for resumable transfer and checksum verification.

## Verification status

Source changes are committed, but the Android APK build and a real Minecraft installation/boot have not been verified by a completed device run.
