# Step 173 — Launch Diagnostics & Observability

Step 173 adds a small, UI-friendly observation layer around the Minecraft process lifecycle.

## Added
- `LaunchDiagnostics` classifies common startup failures: Java runtime, classpath, native library, GLFW, memory, mod loader, and process failures.
- `LaunchObservation` provides immutable lifecycle snapshots with PID, exit code, message, and diagnostic category.
- `LaunchObserver` provides a stable callback contract for launcher UI, logs, and future monitoring integration.

## Safety
Diagnostics never replaces the original process error. Classification is advisory and the raw message remains available to callers.

## Current limitation
The observer contract is intentionally separate from the existing process monitor so the next integration step can connect both without coupling UI code to process internals.

APK generation and real Minecraft boot remain unverified until a GitHub Actions build succeeds and a device test reaches the Minecraft/GLFW startup path.
