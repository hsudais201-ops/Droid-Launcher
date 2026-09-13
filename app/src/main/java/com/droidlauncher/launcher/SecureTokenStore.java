package com.droidlauncher.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Small Android Keystore-backed token store. Tokens are never written in plaintext. */
public final class SecureTokenStore {
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "DroidLauncherAuthKey";
    private static final String PREFS = "droid_launcher_auth";
    private static final String TOKEN = "refresh_token";

    private final SharedPreferences prefs;

    public SecureTokenStore(Context context) {
        if (context == null) throw new IllegalArgumentException("context is required");
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveRefreshToken(String token) throws Exception {
        if (token == null || token.trim().isEmpty()) throw new IllegalArgumentException("token is required");
        byte[] iv = new byte[12];
        new java.security.SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(token.trim().getBytes(StandardCharsets.UTF_8));
        prefs.edit()
                .putString(TOKEN, Base64.encodeToString(iv, Base64.NO_WRAP) + "."
                        + Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .apply();
    }

    public String loadRefreshToken() throws Exception {
        String stored = prefs.getString(TOKEN, "");
        if (stored == null || stored.isEmpty()) return "";
        String[] parts = stored.split("\\.", 2);
        if (parts.length != 2) throw new IllegalStateException("Corrupt authentication token");
        byte[] iv = Base64.decode(parts[0], Base64.DEFAULT);
        byte[] encrypted = Base64.decode(parts[1], Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

    public void clear() { prefs.edit().remove(TOKEN).apply(); }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
        keyStore.load(null);
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
        }
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return generator.generateKey();
    }
}
