# Step 189 — Account Manager UI

Droid Launcher now exposes a real account-management layer in the main screen.

## Included

- Account metadata persistence with selected-account state.
- Secure token storage remains isolated in the Android Keystore-backed store.
- Saved authenticated sessions can be restored without putting tokens in UI state or source constants.
- Sign-out clears both the selected account metadata and stored token.
- PLAY requires a saved authenticated session instead of using empty/fake login values.
- Authenticated player name, UUID, and Minecraft access token are passed into launch-argument generation.

## Important configuration

Microsoft interactive sign-in still requires a valid public client ID configured for the application. No client secret or fake client ID is embedded by this step.

## Verification status

Repository code has been updated, but an Android APK build and real Microsoft login/Minecraft boot have not been verified in this environment.
