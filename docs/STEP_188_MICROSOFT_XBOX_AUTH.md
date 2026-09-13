# Step 188 — Microsoft → Xbox Live → XSTS → Minecraft Authentication

Droid Launcher now contains the protocol layer needed to turn a Microsoft access token into a Minecraft Services session.

## Flow

1. Microsoft public-client device-code authentication.
2. Microsoft access token → Xbox Live user token.
3. Xbox Live user token → XSTS token.
4. XSTS token → Minecraft Services access token.
5. Minecraft Services profile lookup.
6. Result is represented by `MinecraftAuthSession` and `AuthenticatedProfile`.

## Configuration

`MicrosoftAuthConfig` requires the Microsoft application client ID. No client secret is stored in the application.

The current project intentionally does not contain a real client ID. A registered public/native Microsoft application must be configured before live sign-in can work.

## Security

Refresh tokens can be stored through the existing Android Keystore-backed `SecureTokenStore`. Access tokens are kept in memory for the session and are not written to the repository.

## Important

Minecraft Services may reject an unregistered application even when the Microsoft and Xbox token exchanges succeed. Live authentication therefore requires a correctly registered Microsoft application for the launcher.
