# Step 161 — Real Launch Engine

## Goal
Move Droid Launcher from launcher UI/performance configuration toward a reliable Minecraft Java launch pipeline.

## Pipeline

Android Surface
→ native renderer
→ GLFW
→ LWJGL
→ Java runtime
→ Minecraft libraries
→ Minecraft main class
→ process monitor

## Required work

- Select Java runtime from Minecraft version requirements.
- Validate runtime before launch.
- Resolve Minecraft version metadata.
- Resolve libraries and native libraries.
- Extract natives for the active ABI.
- Build a deterministic classpath.
- Prepare Minecraft environment variables and working directory.
- Start and monitor the Java process.
- Capture stdout/stderr and crash information.
- Detect ClassNotFoundException, NoClassDefFoundError, UnsatisfiedLinkError, GLFW failures, OOM, and native crashes.
- Provide actionable recovery messages.

## Performance integration

Keep the Step 160 performance modes:

- AUTO
- LOW
- BALANCED
- HIGH

The selected profile must control RAM/JVM arguments and Minecraft graphics settings without breaking version-specific launch requirements.

## Verification rule

Do not claim real Minecraft boot is verified until a GitHub Actions runtime test successfully launches the target Minecraft environment and produces usable diagnostics.
