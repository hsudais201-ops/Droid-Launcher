# Step 175 — Real PLAY Pipeline Integration

The launcher PLAY button now enters the real `MinecraftLaunchManager` pipeline instead of displaying a placeholder toast.

## Flow

1. Validate the saved profile.
2. Discover an available Java runtime or match the explicitly configured executable.
3. Build the expected version JAR classpath entry.
4. Invoke `LaunchUiController` asynchronously.
5. Run `LaunchPreflight` before creating the child process.
6. Start Minecraft without a shell.
7. Attach `MinecraftProcessMonitor` to stdout, stderr, and process exit.
8. Surface lifecycle and diagnostic messages on the Android main thread.
9. STOP requests terminate the child process through the launch manager.

## Safety

- Invalid profiles stop before process creation.
- Missing Java, game directory, natives directory, classpath, or main class are reported as preflight failures.
- UI work stays on Android's main thread; process work is performed off-thread.
- No claim is made that Minecraft boots until an actual device/runtime test reaches the Minecraft/GLFW startup path.

## Current limitation

The default profile is still a placeholder until a real Minecraft installation, managed Java runtime, version metadata, required libraries, assets, and Android-compatible GLFW/LWJGL native stack are present. The PLAY button therefore may correctly report a preflight failure on a fresh installation.
