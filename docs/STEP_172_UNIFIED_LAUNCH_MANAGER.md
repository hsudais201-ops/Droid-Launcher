# Step 172 — Unified Minecraft Launch Manager

Step 172 connects the existing runtime pieces into one guarded launch coordinator.

## Added

- `LaunchState` exposes a stable launch lifecycle: `IDLE`, `PREFLIGHT`, `COMMAND_READY`, `STARTING`, `RUNNING`, `STOPPING`, `STOPPED`, `FAILED`.
- `LaunchPreflight` checks Java executable, game directory, native directory, classpath, and main class before startup.
- `MinecraftLaunchManager` prevents duplicate launches, builds the final JVM/game command, sets `java.library.path`, starts the child process without a shell, and provides controlled shutdown.

## Safety behavior

A failed preflight never reaches `ProcessBuilder.start()`. Invalid command construction is converted into a launch failure with a retained error message. Shutdown first requests a normal process termination and escalates to forced termination when required.

## Current limitation

This manager wires the launch pipeline, but a real Minecraft instance still requires compatible Java runtime files, complete version metadata, downloaded libraries/assets, Android-compatible native GLFW/LWJGL components, and a device test. Source-level integration is not proof of a successful Minecraft boot.

## Next

Step 173 should connect the unified manager to the existing process-monitor/diagnostics layer and surface live launch state and failure reasons in the Android UI.
