# Step 184 — Automatic Install Before Launch

The PLAY button now prepares the requested Minecraft version before attempting to start it.

## Flow

1. Resolve the requested version from Mojang's version manifest.
2. `latest` resolves to Mojang's latest release.
3. Fetch and validate the selected version metadata and asset index.
4. Build the complete verified download plan.
5. Download/reuse artifacts through the existing SHA-1 checked orchestrator.
6. Persist installation progress in `InstallationStateStore`.
7. Launch the installed version using the main class from version metadata.

The launcher does not claim successful Minecraft boot unless the existing process-launch pipeline actually starts the JVM process.
