package com.droidlauncher.launcher;

/** Authentication session passed to the Minecraft process; never uses hard-coded credentials. */
public final class MinecraftAuthSession {
    private final AuthenticatedProfile profile;
    private final String minecraftAccessToken;

    public MinecraftAuthSession(AuthenticatedProfile profile, String minecraftAccessToken) {
        if (profile == null) throw new IllegalArgumentException("profile is required");
        if (minecraftAccessToken == null || minecraftAccessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("minecraft access token is required");
        }
        this.profile = profile;
        this.minecraftAccessToken = minecraftAccessToken.trim();
    }

    public AuthenticatedProfile getProfile() { return profile; }
    public String getMinecraftAccessToken() { return minecraftAccessToken; }
}
