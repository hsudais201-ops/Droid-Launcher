package com.droidlauncher.launcher;

import android.content.Context;
import android.content.SharedPreferences;

/** Persists non-secret Minecraft account metadata and the currently selected account. */
public final class AccountStore {
    private static final String PREFS = "droid_launcher_accounts";
    private static final String SELECTED = "selected";
    private static final String ID = "id.";
    private static final String NAME = "name.";
    private static final String UUID = "uuid.";

    private final SharedPreferences prefs;

    public AccountStore(Context context) {
        if (context == null) throw new IllegalArgumentException("context is required");
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(AuthenticatedProfile profile) {
        if (profile == null) throw new IllegalArgumentException("profile is required");
        String id = profile.getAccountId();
        prefs.edit()
                .putString(ID + id, id)
                .putString(NAME + id, profile.getDisplayName())
                .putString(UUID + id, profile.getUuid())
                .putString(SELECTED, id)
                .apply();
    }

    public String getSelectedAccountId() {
        return prefs.getString(SELECTED, "");
    }

    public String getSelectedDisplayName() {
        String id = getSelectedAccountId();
        return id.isEmpty() ? "" : prefs.getString(NAME + id, "");
    }

    public String getSelectedUuid() {
        String id = getSelectedAccountId();
        return id.isEmpty() ? "" : prefs.getString(UUID + id, "");
    }

    public void select(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) throw new IllegalArgumentException("account id is required");
        String id = accountId.trim();
        if (!prefs.contains(ID + id)) throw new IllegalArgumentException("Unknown account: " + id);
        prefs.edit().putString(SELECTED, id).apply();
    }

    public void remove(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) return;
        String id = accountId.trim();
        SharedPreferences.Editor editor = prefs.edit()
                .remove(ID + id)
                .remove(NAME + id)
                .remove(UUID + id);
        if (id.equals(getSelectedAccountId())) editor.remove(SELECTED);
        editor.apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
