# Step 185 — Real Minecraft Classpath and Native Preparation

Droid Launcher now prepares a launch using the installed Minecraft client JAR plus resolved JVM library JARs instead of launching with the client JAR alone.

## Added

- `MinecraftLibraryMetadataParser` parses Mojang `libraries` metadata and selects Android-relevant Linux native classifiers when present.
- `MinecraftLaunchClasspath` builds a deterministic client-plus-library classpath and returns native candidates.
- `MinecraftNativePreparer` validates the device ABI, selects compatible native artifacts, extracts `.so` files, and verifies they are readable before launch.
- `FullInstallationPlanner` now downloads both ordinary library artifacts and native classifiers described by the version JSON.
- `MainActivity` now prepares the complete classpath and native directory before calling the real process launcher.

## Safety

Every library download still requires a URL and SHA-1. Library paths are canonicalized and rejected when they escape the `libraries` directory. Native selection fails rather than silently accepting an unknown ABI.

## Current limitation

Native compatibility depends on what the selected Minecraft version actually publishes. A version that does not contain a native artifact compatible with the Android device ABI will fail during native preparation rather than attempting an unsafe launch.
