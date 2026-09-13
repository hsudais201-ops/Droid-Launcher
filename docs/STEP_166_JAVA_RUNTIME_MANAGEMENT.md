# Step 164 — Java Runtime Management

The launcher now has a Java runtime layer that is independent of Minecraft process launching.

## Added
- `JavaRuntime`: immutable executable/version descriptor.
- `JavaRuntimeDetector`: searches PATH and launcher-owned runtime locations and invokes `java -version` safely.
- `JavaRequirement`: validates a runtime against a required major-version range.

## Important runtime behavior
The detector does not assume that the Android system has a usable Java runtime. A future runtime installer/downloader will place managed runtimes in the launcher data directory; this detector can then discover them.

Minecraft profile metadata should provide the required Java major-version range before launch. The launcher must refuse to start a profile when no compatible runtime is available instead of producing a later `ClassNotFoundException`, `NoClassDefFoundError`, or JVM startup failure.

## Current limitation
The Android shell does not yet bundle a full managed JRE, and real Minecraft Java execution is not verified. Step 165/166 should add managed runtime provisioning, then library/classpath resolution and process monitoring.
