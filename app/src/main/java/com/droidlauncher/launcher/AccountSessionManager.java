package com.droidlauncher.launcher;

import android.content.Context;

/** Coordinates the selected Minecraft account with secure authentication storage. */
public final class AccountSessionManager {
    private final AccountStore accountStore;
    private final SecureTokenStore tokenStore;

    public AccountSessionManager(Context context) {
        if (context == null) throw new IllegalArgumentException("context is required");
        this.accountStore = new AccountStore(context);
        this.tokenStore = new SecureTokenStore(context);
    }

    public AccountStore getAccountStore() { return accountStore; }

    public MinecraftAuthSession loadSavedSession() {
        String accountId = accountStore.getSelectedAccountId();
        if (accountId.isEmpty()) return null;
        try {
            String token = tokenStore.loadRefreshToken();
            if (token == null || token.isEmpty()) return null;
            String name = accountStore.getSelectedDisplayName();
            String uuid = accountStore.getSelectedUuid();
            if (name.isEmpty()) return null;
            AuthenticatedProfile profile = new AuthenticatedProfile(accountId, name, token, uuid, 0L);
            return new MinecraftAuthSession(profile, token);
        } catch (Exception e) {
            return null;
        }
    }

    public void saveSession(MinecraftAuthSession session) throws Exception {
        if (session == null) throw new IllegalArgumentException("session is required");
        accountStore.save(session.getProfile());
        tokenStore.saveRefreshToken(session.getMinecraftAccessToken());
    }

    public void signOut() {
        tokenStore.clear();
        accountStore.clearAll();
    }
}
