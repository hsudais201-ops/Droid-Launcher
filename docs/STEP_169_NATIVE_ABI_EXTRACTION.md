# Step 169 — Native Library Extraction and Android ABI Mapping

Step 169 adds the preparation layer required before GLFW/LWJGL can load native code.

## Added

- `NativeAbi` normalizes Android ABI values for ARM64, ARM32, x86_64, and x86.
- `NativeLibraryResolver` selects native dependencies compatible with the detected ABI and rejects unknown ABI targets instead of silently continuing.
- `NativeLibraryExtractor` extracts `.so` files from library JARs into a controlled directory and blocks unsafe output paths.
- Extracted libraries are checked for existence, readability, and non-zero size before native loading.

## Important Android limitation

ABI matching only proves that a native binary is intended for the selected CPU family. It does **not** convert desktop native libraries into Android libraries. In particular, ordinary desktop LWJGL/GLFW Linux binaries cannot be assumed to run on ARM Android.

The later native-runtime steps therefore still need Android-compatible GLFW/LWJGL native builds (or an explicitly supported translation/runtime layer) before real Minecraft rendering can be claimed as working.

## Safety rules

- Never extract a native entry outside the managed native directory.
- Never continue with an unknown device ABI.
- Never load a missing or empty extracted `.so`.
- Keep native extraction separate from download and verification so checksum validation can happen before extraction.

## Verification status

This commit adds the source implementation only. The repository's GitHub Actions APK build and a real Minecraft boot remain unverified until an actual workflow run completes and a device/runtime test reaches GLFW/LWJGL initialization.
