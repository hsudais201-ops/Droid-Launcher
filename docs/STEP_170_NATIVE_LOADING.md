# Step 170 — Native Loading and GLFW/LWJGL Bridge

This step connects the prepared native directory to the future Minecraft Java launch environment.

## Added

- `NativeLibraryLoader` safely loads one native `.so` from a controlled directory and converts `UnsatisfiedLinkError` into a launch-layer `IOException`.
- `NativeLaunchEnvironment` validates the detected Android ABI and exposes the native directory as the Java library path.
- `GLFWRuntimeBridge` provides a guarded entry point for Android-compatible GLFW initialization.

## Safety

- Native paths are canonicalized and must remain inside the managed native directory.
- Missing and empty native files are rejected before `System.load()`.
- Unknown Android ABIs are rejected.
- Native loading failures are surfaced instead of being swallowed.

## Critical limitation

This bridge does not make desktop LWJGL/GLFW binaries compatible with Android. A real Minecraft boot still requires an Android-compatible native GLFW/LWJGL stack and the corresponding Java-side integration.

## Verification

Source integration is committed, but GitHub Actions APK generation and real device Minecraft/GLFW initialization remain unverified until an actual build and runtime test succeeds.
