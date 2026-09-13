# Step 171 — Minecraft Java Process Launch

Step 171 adds the process boundary between the launcher's prepared runtime data and the Minecraft Java entry point.

## Added

- `MinecraftCommand`: immutable Java executable, working directory, and argument model.
- `MinecraftProcessLauncher`: starts Java directly with `ProcessBuilder` and no shell interpolation, while consuming stdout/stderr asynchronously.
- `MinecraftProcessLauncher.combineJvmAndGameArguments(...)`: places launcher-owned `-cp <classpath>` before the Minecraft main class and then appends game arguments.
- `MinecraftProcessEnvironment`: creates an explicit native-library environment description.

## Safety and reliability

- The Java executable must exist before a process can be created.
- The working directory must already exist.
- Null-byte arguments are rejected.
- Shell execution is not used, avoiding quoting and command-injection problems.
- Both child-process output streams are consumed asynchronously to reduce the risk of a blocked child process.
- The launcher remains responsible for validating Java version, classpath contents, native compatibility, and final JVM/game arguments before calling `launch(...)`.

## Important runtime limitation

This step creates the real OS process-launch boundary, but it does **not** prove that Minecraft can run on Android. A successful Java process start still depends on an Android-compatible Java runtime, compatible LWJGL/GLFW natives, the correct Minecraft version metadata/assets, and a working Android rendering bridge.

## Verification status

Source changes are committed. APK generation, device execution, GLFW initialization, and real Minecraft boot still require an actual build and runtime test.

## Next

Step 172 should connect this process boundary to the existing profile/runtime/JVM/classpath/native preparation into one validated `MinecraftLaunchManager` transaction with preflight checks and structured launch states.
