# Step 163 — Minecraft Version Download & Cache

This step adds the first reusable artifact layer for the real Minecraft launch pipeline.

## Added
- `MinecraftVersion`: immutable version metadata (`id`, `type`, metadata URL, SHA-1).
- `DownloadManager`: HTTP download helper with resume support when the server accepts byte ranges.
- `Sha1Verifier`: dependency-free SHA-1 verification before an artifact is trusted.

## Safety rules
- Network I/O must run away from the Android main thread.
- A downloaded file is not considered valid when the expected checksum is missing or does not match.
- A server that ignores a Range request is handled by restarting the file rather than corrupting it.
- The next layer should store metadata in the app cache/data directory and add atomic temp-file replacement.

## Step 164
Java runtime discovery and validation, followed by mapping the selected Minecraft version to its required Java major version.

## Status
This is implementation code only. It does not yet download official Minecraft metadata automatically and does not claim a successful Minecraft launch.
