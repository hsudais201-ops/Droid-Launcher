package com.droidlauncher.launcher;

import android.content.Context;

/** Restores a real Minecraft session from the encrypted Microsoft refresh token. */
public final class MinecraftSessionRestorer {
    private final SecureTokenStore tokenStore;
    private final AccountStore accountStore;

    public MinecraftSessionRestorer(Context context) {
        if (context == null) throw new IllegalArgumentException("context is required");
        tokenStore = new SecureTokenStore(context);
        accountStore = new AccountStore(context);
    }

    /**
     * Refreshes Microsoft authentication, performs the Xbox/XSTS/Minecraft exchange, verifies
     * ownership/profile, and returns a fresh in-memory Minecraft session.
     */
    public MinecraftAuthSession restore(MicrosoftAuthConfig config) throws Exception {
        String refreshToken = tokenStore.loadRefreshToken();
        if (refreshToken == null || refreshToken.trim().isEmpty()) return null;
        MicrosoftOAuthClient.MicrosoftTokenResponse microsoft =
                new MicrosoftTokenRefresher().refresh(config, refreshToken);
        if (!microsoft.getRefreshToken().isEmpty()) {
            tokenStore.saveRefreshToken(microsoft.getRefreshToken());
        }
        MinecraftAuthSession session = new MinecraftXboxAuthenticator().authenticate(microsoft.getAccessToken());
        accountStore.save(session.getProfile());
        return session;
    }
}
