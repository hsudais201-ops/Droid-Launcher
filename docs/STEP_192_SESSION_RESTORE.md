# Step 192 — Persistent Microsoft Session Restore

Droid Launcher now has a reusable session-restoration layer for account persistence.

## Added

- `MicrosoftTokenRefresher` exchanges the encrypted Microsoft refresh token for a fresh Microsoft access token.
- `MinecraftSessionRestorer` rebuilds the complete Xbox Live → XSTS → Minecraft Services session.
- Rotating Microsoft refresh tokens are written back to the encrypted Android Keystore-backed store.
- The restored Minecraft access token remains in memory; it is not persisted as a long-lived credential.
- Minecraft ownership/profile verification is performed again when restoring the session.

## Security boundary

Only the Microsoft refresh token is persisted by the current account flow. Minecraft access tokens are short-lived session data and should not replace the refresh token in secure storage.

## Integration boundary

The restoration service is now available to the activity/session layer. The next UI integration should invoke it during startup, restore `minecraftSession`, and fall back to the sign-in flow when refresh or Minecraft verification fails.

Real Minecraft boot is still not claimed as verified until the GitHub Actions APK build and an actual device launch both succeed.
