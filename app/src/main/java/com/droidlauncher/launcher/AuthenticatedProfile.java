package com.droidlauncher.launcher;

/** Minecraft identity information used to build a real authenticated launch session. */
public final class AuthenticatedProfile {
    private final String accountId;
    private final String displayName;
    private final String accessToken;
    private final String uuid;
    private final long expiresAtMillis;

    public AuthenticatedProfile(String accountId, String displayName, String accessToken,
                                String uuid, long expiresAtMillis) {
        if (accountId == null || accountId.trim().isEmpty()) throw new IllegalArgumentException("accountId is required");
        if (displayName == null || displayName.trim().isEmpty()) throw new IllegalArgumentException("displayName is required");
        if (accessToken == null || accessToken.trim().isEmpty()) throw new IllegalArgumentException("accessToken is required");
        this.accountId = accountId.trim();
        this.displayName = displayName.trim();
        this.accessToken = accessToken.trim();
        this.uuid = uuid == null ? "" : uuid.trim();
        this.expiresAtMillis = expiresAtMillis;
    }

    public String getAccountId() { return accountId; }
    public String getDisplayName() { return displayName; }
    public String getAccessToken() { return accessToken; }
    public String getUuid() { return uuid; }
    public long getExpiresAtMillis() { return expiresAtMillis; }

    public boolean isExpired(long nowMillis) {
        return expiresAtMillis > 0L && nowMillis >= expiresAtMillis;
    }
}
