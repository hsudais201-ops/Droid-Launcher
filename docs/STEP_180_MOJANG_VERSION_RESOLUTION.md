# Step 180 — Mojang Version Resolution

Droid Launcher now has a single installation-preparation service that:

1. Fetches Mojang's version manifest from `piston-meta.mojang.com`.
2. Finds the requested Minecraft version ID.
3. Fetches that version's JSON metadata.
4. Validates the version ID returned by the metadata.
5. Fetches the version's asset index when present.
6. Verifies the asset-index SHA-1 when supplied by Mojang.
7. Passes version JSON + asset-index JSON into `FullInstallationPlanner`.
8. Returns one `InstallationPlan` containing metadata and every download task.

This keeps network resolution separate from the UI and gives later download/execution steps a deterministic installation plan.

## Not yet claimed

This step does not claim that a Minecraft build has successfully booted on Android. Actual artifact downloading, native extraction/loading, JVM startup, GLFW initialization, and Minecraft boot still require runtime verification on a supported device.
