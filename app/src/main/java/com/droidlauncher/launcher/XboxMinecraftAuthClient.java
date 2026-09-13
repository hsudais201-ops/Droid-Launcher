package com.droidlauncher.launcher;

import java.io.IOException;

/**
 * Backward-compatible facade for the canonical Minecraft authentication implementation.
 * The launcher uses MinecraftXboxAuthenticator for the complete verified exchange.
 */
public final class XboxMinecraftAuthClient {
    public MinecraftAuthSession exchange(String microsoftAccessToken) throws IOException {
        return new MinecraftXboxAuthenticator().authenticate(microsoftAccessToken);
    }
}
