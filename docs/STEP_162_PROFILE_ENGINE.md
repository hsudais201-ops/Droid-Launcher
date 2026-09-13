# Step 162 — Minecraft Profile Engine

Step 162 adds the first real runtime-facing data layer behind the launcher UI.

## Added
- `MinecraftProfile`: immutable profile model for id, Minecraft version, game directory, Java executable, and RAM limits.
- `ProfileStore`: persists the selected profile in Android `SharedPreferences` using JSON.
- `ProfileValidator`: rejects missing/invalid profile data before launch.
- `MainActivity`: loads the saved profile, creates a safe default profile when none exists, displays its status, and validates it when PLAY is pressed.

## Important limitation
The default profile uses version `latest` as a placeholder and does not download or launch Minecraft yet. Java, libraries, assets, native extraction, classpath construction, and the actual Minecraft process are still separate implementation stages.

## Next stage
Step 163 should implement version metadata resolution and a download/cache layer, with strict checksums and resumable downloads before the process launcher is connected.
