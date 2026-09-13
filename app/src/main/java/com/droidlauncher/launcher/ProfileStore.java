package com.droidlauncher.launcher;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/** Small persistent store for the selected Minecraft profile. */
public final class ProfileStore {
    private static final String PREFS = "droid_launcher_profiles";
    private static final String KEY_PROFILE = "selected_profile";

    private final SharedPreferences preferences;

    public ProfileStore(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(MinecraftProfile profile) {
        JSONObject json = new JSONObject();
        try {
            json.put("id", profile.getId());
            json.put("version", profile.getVersion());
            json.put("gameDirectory", profile.getGameDirectory());
            json.put("javaExecutable", profile.getJavaExecutable());
            json.put("minRamMb", profile.getMinRamMb());
            json.put("maxRamMb", profile.getMaxRamMb());
            preferences.edit().putString(KEY_PROFILE, json.toString()).apply();
        } catch (JSONException error) {
            throw new IllegalStateException("Unable to encode Minecraft profile", error);
        }
    }

    public MinecraftProfile load() {
        String raw = preferences.getString(KEY_PROFILE, null);
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            JSONObject json = new JSONObject(raw);
            return new MinecraftProfile(
                    json.getString("id"),
                    json.getString("version"),
                    json.optString("gameDirectory", ""),
                    json.optString("javaExecutable", ""),
                    json.optInt("minRamMb", 512),
                    json.optInt("maxRamMb", 2048)
            );
        } catch (JSONException | IllegalArgumentException error) {
            preferences.edit().remove(KEY_PROFILE).apply();
            return null;
        }
    }

    public void clear() {
        preferences.edit().remove(KEY_PROFILE).apply();
    }
}
