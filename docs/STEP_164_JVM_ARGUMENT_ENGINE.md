# Step 164 — Version-Aware JVM / Game Argument Engine

## Goal
Make launch arguments deterministic and safer across legacy, modern, inherited, and newer Minecraft versions.

## Changes
- Shell-free argument tokenization preserves quoted values without invoking a shell.
- Modern JSON JVM/game arguments and legacy `minecraftArguments` strings use one resolver.
- `${classpath}` placeholders are ignored because the launcher owns `-cp` construction.
- Illegal control characters are rejected before Java process creation.
- User-supplied `-Xmx`, `-Xms`, `-cp`, `-classpath`, and Java/native path overrides cannot conflict with launcher-managed values.
- The final JVM and game argument sections are validated before process startup.
- Launcher-brand substitutions use `droid-launcher`.
- Minecraft 26.x falls back to Java 25 when a manifest omits `javaVersion`.

## Compatibility
Minecraft 26.1 officially requires Java 25. Explicit `javaVersion.majorVersion` metadata remains authoritative when it is present.

## Verification
Local tokenizer/validator checks pass. Full Android/NDK build and real Minecraft boot remain dependent on GitHub Actions and runtime verification.
