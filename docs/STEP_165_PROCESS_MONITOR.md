# Step 165 — Live Minecraft Process Monitoring

## Goal
Make embedded-JVM Minecraft launches observable in real time without treating the Android launcher process as a normal child process.

## Implemented

- `MinecraftProcessMonitor` runs concurrently with the synchronous `NativeGameBridge.launchJava()` call.
- Minecraft stdout/stderr is tailed incrementally from the launch log.
- Structured events detect classpath, native, GLFW, Java-version, memory, mod-loader, and JVM/native crash signatures.
- Renderer heartbeat stalls produce diagnostics but do not automatically kill the embedded JVM.
- JVM state transitions are tracked through the native bridge.
- Monitor events are persisted to `logs/diagnostics/step165-events.log`.
- Observation is bounded so a dead bridge does not leave an infinite monitor coroutine.
- `MinecraftLaunchManager` starts/stops the monitor in the same lifecycle as the embedded JLI launch.

## Safety

The monitor is diagnostic only. Clean JVM shutdown remains the responsibility of the existing `requestJavaStop()` path. This avoids racing JLI/GLFW/EGL teardown from a background watchdog.

## Verification

The monitor source compiles successfully when isolated against the existing bridge/logger interfaces. Static integration checks confirm the production launch manager starts and stops the monitor around the embedded JLI call.

A full Android Gradle build and real Minecraft boot are still not claimed as verified until GitHub Actions completes the required build and runtime tests.
