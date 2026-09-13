package com.droidlauncher.launcher;

import java.io.IOException;
import java.util.UUID;

/** Explicit offline/demo authentication session; never used for Microsoft accounts. */
public final class OfflineMinecraftAuth implements MinecraftAuthentication {
    private final SecureTokenStore tokenStore;
    private final String playerName;

    public OfflineMinecraftAuth(SecureTokenStore tokenStore, String playerName) {
        if (tokenStore == null) throw new IllegalArgumentException("tokenStore is required");
        if (playerName == null || playerName.trim().isEmpty()) throw new IllegalArgumentException("playerName is required");
        this.tokenStore = tokenStore;
        this.playerName = playerName.trim();
    }

    @Override public MinecraftAuthSession authenticate() throws IOException {
        String token;
        try {
            token = tokenStore.loadRefreshToken();
        } catch (Exception e) {
            throw new IOException("Unable to read saved authentication state", e);
        }
        if (token == null || token.isEmpty()) {
            throw new IOException("No saved authenticated session");
        }
        String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString().replace("-", "");
        AuthenticatedProfile profile = new AuthenticatedProfile(
                "offline:" + playerName.toLowerCase(java.util.Locale.ROOT),
                playerName,
                token,
                uuid,
                0L);
        return new MinecraftAuthSession(profile, token);
    }

    @Override public void signOut() {
        tokenStore.clear();
    }
}
