package com.droidlauncher.launcher;

import android.content.Context;

/** Coordinates selected Minecraft account metadata with secure Microsoft refresh-token storage. */
public final class AccountSessionManager {
    private final AccountStore accountStore;
    private final SecureTokenStore tokenStore;

    public AccountSessionManager(Context context) {
        if (context == null) throw new IllegalArgumentException("context is required");
        this.accountStore = new AccountStore(context);
        this.tokenStore = new SecureTokenStore(context);
    }

    public AccountStore getAccountStore() { return accountStore; }

    /**
     * Returns the persisted Minecraft profile metadata, never pretending a Microsoft refresh token
     * is a Minecraft access token. The actual launch session must be rebuilt through
     * MinecraftSessionRestorer.
     */
    public AuthenticatedProfile loadSelectedProfile() {
        String accountId = accountStore.getSelectedAccountId();
        if (accountId.isEmpty()) return null;
        String name = accountStore.getSelectedDisplayName();
        String uuid = accountStore.getSelectedUuid();
        if (name.isEmpty()) return null;
        return new AuthenticatedProfile(accountId, name, "metadata-only", uuid, 0L);
    }

    /** Persist only non-secret account metadata; the Minecraft access token is intentionally transient. */
    public void saveProfileMetadata(AuthenticatedProfile profile) {
        if (profile == null) throw new IllegalArgumentException("profile is required");
        accountStore.save(profile);
    }

    /** Returns whether an encrypted Microsoft refresh token exists. */
    public boolean hasRefreshToken() {
        try {
            String token = tokenStore.loadRefreshToken();
            return token != null && !token.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public void signOut() {
        tokenStore.clear();
        accountStore.clearAll();
    }
}
