# Step 190 — Microsoft Device Sign-In

Droid Launcher now has the foundation for interactive Microsoft public-client authentication.

## Added

- Build-time `MICROSOFT_CLIENT_ID` configuration via Gradle project property.
- Android `INTERNET` permission for authentication and Minecraft downloads.
- Microsoft OAuth device-code request and polling.
- Correct handling of `authorization_pending` and `slow_down` OAuth responses.
- Non-blocking sign-in coordinator so authentication does not run on the Android UI thread.
- Device-code dialog with browser-open and code-copy actions.
- Secure refresh-token persistence through the existing Android Keystore-backed store.
- Sign-out action that clears the stored refresh token.

## Build configuration

Provide the public Microsoft client ID as a Gradle property, for example:

`./gradlew :app:assembleDebug -PMICROSOFT_CLIENT_ID=<your-public-client-id>`

Do not place a client secret, password, or refresh token in source control.

## Current boundary

This step completes Microsoft device-code OAuth. A successful Microsoft access token is not by itself a Minecraft access token. The existing Xbox Live, XSTS, and Minecraft Services exchange must still produce the final Minecraft profile/session before online launch is considered complete.
