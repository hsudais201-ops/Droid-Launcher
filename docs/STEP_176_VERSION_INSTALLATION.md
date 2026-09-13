# Step 176 — Minecraft Version Manifest and Installation Pipeline

This step adds the metadata and filesystem layer needed to turn a selected Minecraft version into a reproducible local installation.

## Added
- `VersionManifestClient` fetches the Mojang version manifest and parses version entries.
- `VersionMetadata` stores client JAR, main class, asset index, and library repository metadata.
- `VersionMetadataParser` parses the version JSON without assuming that files are already installed.
- `VersionInstallation` centralizes safe paths for versions, libraries, and assets.

## Safety
- Version IDs are rejected when they contain path traversal or path separators.
- Network work is explicit and remains outside the Android UI thread.
- Client and asset downloads must be checksum-verified by the existing download/cache layer before they are accepted for launch.

## Current limitation
This step prepares metadata and canonical locations only. Full library/asset download orchestration and an end-to-end verified Minecraft boot still require actual runtime files and Android-compatible GLFW/LWJGL native libraries.
