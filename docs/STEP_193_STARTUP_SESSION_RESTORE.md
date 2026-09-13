# Step 193 — Startup Minecraft Session Restore

Droid Launcher now restores a saved Microsoft-authenticated Minecraft session during activity startup.

## Added

- Startup invokes `MinecraftSessionRestorer` when a build-time Microsoft client ID is available.
- The encrypted Microsoft refresh token is exchanged for a fresh Microsoft access token.
- The existing Xbox Live → XSTS → Minecraft Services flow rebuilds the transient Minecraft session.
- The restored Minecraft account is shown in the launcher without requiring another device-code sign-in when the refresh token is still valid.
- PLAY continues to require a real, non-expired Minecraft access token.
- `AccountSessionManager` no longer fabricates a Minecraft session from a Microsoft refresh token and now keeps persisted account metadata separate from transient launch credentials.

## Failure behavior

If restoration fails, Droid Launcher keeps the saved authorization metadata but returns to the normal Microsoft sign-in path. No Minecraft access token is persisted as a long-lived credential.

## Verification boundary

CI build success and real Minecraft boot on an Android device are still separate verification steps and are not claimed here unless the corresponding tests complete successfully.
