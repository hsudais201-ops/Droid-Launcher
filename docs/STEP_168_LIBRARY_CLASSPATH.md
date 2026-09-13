# Step 168 — Library Resolution & Classpath Construction

This step adds the deterministic library layer between Minecraft version metadata and the future Java launch command.

## Added
- `LibraryDependency`: artifact metadata, checksum, platform hints, resolved file, and native marker.
- `LibraryRule`: reusable OS/architecture matching primitive.
- `LibraryResolver`: filters candidate libraries to the target platform and only returns existing resolved artifacts.
- `ClasspathBuilder`: rejects missing files and creates a duplicate-free, deterministic classpath ordered by absolute path.

## Design rules
- Library selection is platform-aware and does not assume desktop-only artifacts belong on Android.
- Classpath generation is deterministic so launch diagnostics are reproducible.
- Missing artifacts fail before process launch instead of becoming late class-loading errors.
- Checksum verification remains the responsibility of the download/cache layer before an artifact enters the resolved set.

## Current limitation
The parser for Mojang version JSON, dependency download orchestration, native extraction, and end-to-end Minecraft launch are still separate stages.

## Next
Step 169: native-library extraction and ABI mapping, followed by the GLFW/LWJGL bridge and real launch integration.
