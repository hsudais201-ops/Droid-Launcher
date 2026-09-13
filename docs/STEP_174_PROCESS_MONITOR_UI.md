# Step 174 — Process Monitor and Live Launch UI

Step 174 connects the child-process lifecycle to the launch observer layer and provides an Android-safe UI bridge.

## Added

- `MinecraftProcessMonitor` consumes stdout/stderr concurrently and reports process exit codes.
- `LaunchUiController` moves launch observations back to the Android main thread.
- Diagnostic categories remain available through `LaunchDiagnostics` so raw process errors are not hidden.

## Behavior

- Running processes report `RUNNING` with their PID when the platform exposes it.
- Each non-empty stdout/stderr line is surfaced as a live launch detail.
- Exit code `0` is treated as a normal stop; non-zero exits are reported as failures.
- Stream and monitor errors are reported instead of being silently swallowed.
- No shell is introduced; the process remains created through Java `ProcessBuilder`.

## Verification status

Source integration is committed, but APK generation and a real Minecraft boot are still unverified until GitHub Actions succeeds and a device test reaches the Minecraft/GLFW runtime.
