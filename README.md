# Droid Launcher

Minecraft Java Android launcher project.

## Current baseline
- Landscape-first Droid Launcher UI
- Performance-tuning architecture reserved for Auto / Low / Balanced / High modes
- Adaptive RAM/JVM tuning planned
- Minecraft `options.txt` tuning planned
- GitHub Actions cloud build

## Step 161 — Real Launch Engine foundation
This repository now contains the first real Android application foundation:

1. Android app module and namespace
2. Landscape launcher entry activity
3. Droid Launcher branding
4. Java 17 Android compilation target
5. Wrapper-free GitHub Actions build using Gradle 9.6.0
6. Debug APK artifact upload

The Minecraft runtime pipeline remains the next implementation layer: profile/version management, Java runtime validation, library and asset resolution, native extraction, classpath construction, GLFW/LWJGL setup, process launch, monitoring, crash diagnostics, and recovery.

## Build status
The project has a configured cloud-build path, but APK generation is **not claimed as verified** until GitHub Actions completes successfully. Real Minecraft boot is also **not yet verified**.
