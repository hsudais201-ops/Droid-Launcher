# Droid Launcher

Minecraft Java Android launcher project.

## Current baseline
- Step 160 performance system: Auto / Low / Balanced / High
- Adaptive RAM and JVM tuning
- Minecraft `options.txt` performance tuning
- Landscape-first launcher UI
- Droid Launcher branding
- GitHub Actions/cloud-build direction

## Step 161 — Real Launch Engine
The next development phase focuses on the actual Minecraft launch pipeline:

1. Minecraft version/profile management
2. Java runtime selection and validation
3. Library and asset resolution
4. Native library extraction
5. Classpath construction
6. GLFW/LWJGL environment setup
7. Minecraft process launch and monitoring
8. Crash diagnostics and automatic recovery

The Step 160 source remains the baseline while the project is migrated from the legacy CraftDroid repository into this repository.

## Build status
APK generation and real Minecraft boot are **not yet claimed as verified** until GitHub Actions completes the required build and runtime tests.
