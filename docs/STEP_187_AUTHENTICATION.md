# Step 187 — Microsoft/Minecraft Authentication Foundation

Droid Launcher now has a real authentication boundary instead of hard-coded login values.

## Added

- `AuthenticatedProfile` stores the Minecraft account id, player name, UUID and expiry metadata.
- `MinecraftAuthSession` carries the authenticated Minecraft access token into the launch layer.
- `MinecraftAuthentication` defines the sign-in/sign-out contract for a Microsoft-backed implementation.
- `SecureTokenStore` uses the Android Keystore with AES-GCM to protect the refresh token at rest.

## Security

The Android client is a public client and must not contain a Microsoft client secret. A production Microsoft sign-in implementation should use Microsoft's supported public-client OAuth flow and keep refresh tokens out of plain `SharedPreferences` storage. See Microsoft guidance for Android public/native authentication and device-code support.

## Current limitation

A real Microsoft account exchange still requires a registered public client application and the corresponding OAuth configuration. This step intentionally does not embed a fake client id, secret, or fake access token.

The next authentication stage can add the interactive Microsoft sign-in flow and the Xbox Live → XSTS → Minecraft Services token exchange.
