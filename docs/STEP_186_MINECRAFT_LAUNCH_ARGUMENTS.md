# Step 186 — Minecraft Launch Arguments

Droid Launcher now prepares a real Minecraft client argument set after installation.

## Runtime arguments

The launcher supplies:

- `--username`
- `--version`
- `--gameDir`
- `--assetsDir`
- `--assetIndex` when available
- `--uuid`
- `--accessToken`
- `--userType legacy`
- `--versionType release`

The username is derived from the active launcher profile ID for the current offline path. The access token is deliberately a placeholder (`0`) until authenticated account support is added.

## JVM arguments

The launch path also supplies:

- `-Djava.library.path=<natives>`
- `-Xms<profile minimum RAM>M`
- `-Xmx<profile maximum RAM>M`

## Classpath

The client JAR is combined with the verified installed JVM libraries discovered from the version JSON. Native libraries are prepared separately and exposed through `java.library.path`.

This completes the argument/classpath handoff. Real Minecraft boot still requires an Android-compatible Java runtime, compatible LWJGL/GLFW native stack, and actual runtime verification on a supported device.
