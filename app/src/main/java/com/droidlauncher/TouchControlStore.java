package com.droidlauncher;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TouchControlStore {
    private static final String PREFS = "touch_controls";
    private static final String KEY_LAYOUT = "layout";

    private final SharedPreferences preferences;

    public TouchControlStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<TouchControlConfig> load() {
        String raw = preferences.getString(KEY_LAYOUT, "");
        if (raw == null || raw.trim().isEmpty()) return defaults();
        try {
            JSONArray array = new JSONArray(raw);
            List<TouchControlConfig> result = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                TouchControlAction action;
                try { action = TouchControlAction.valueOf(o.optString("action", "JUMP")); }
                catch (IllegalArgumentException e) { action = TouchControlAction.JUMP; }
                result.add(new TouchControlConfig(
                        o.optString("id", UUID.randomUUID().toString()),
                        o.optString("label", action.getLabel()), action,
                        (float) o.optDouble("x", 0.82), (float) o.optDouble("y", 0.68),
                        (float) o.optDouble("width", 0.10), (float) o.optDouble("height", 0.16),
                        (float) o.optDouble("opacity", 0.82), o.optString("shape", "circle"),
                        o.optBoolean("visible", true)));
            }
            return result.isEmpty() ? defaults() : result;
        } catch (Exception e) {
            return defaults();
        }
    }

    public void save(List<TouchControlConfig> controls) {
        JSONArray array = new JSONArray();
        try {
            for (TouchControlConfig c : controls) {
                JSONObject o = new JSONObject();
                o.put("id", c.id);
                o.put("label", c.label);
                o.put("action", c.action.name());
                o.put("x", c.x);
                o.put("y", c.y);
                o.put("width", c.width);
                o.put("height", c.height);
                o.put("opacity", c.opacity);
                o.put("shape", c.shape);
                o.put("visible", c.visible);
                array.put(o);
            }
            preferences.edit().putString(KEY_LAYOUT, array.toString()).apply();
        } catch (Exception ignored) {
            // Keep the last valid layout when serialization fails.
        }
    }

    public void reset() {
        preferences.edit().remove(KEY_LAYOUT).apply();
    }

    private List<TouchControlConfig> defaults() {
        List<TouchControlConfig> list = new ArrayList<>();
        list.add(control("joystick", "MOVE", TouchControlAction.JOYSTICK, 0.03f, 0.66f, 0.18f, 0.27f, "circle"));
        list.add(control("jump", "JUMP", TouchControlAction.JUMP, 0.82f, 0.64f, 0.10f, 0.16f, "circle"));
        list.add(control("sneak", "SNEAK", TouchControlAction.SNEAK, 0.72f, 0.78f, 0.09f, 0.12f, "rounded"));
        list.add(control("attack", "ATTACK", TouchControlAction.ATTACK, 0.86f, 0.43f, 0.10f, 0.16f, "circle"));
        list.add(control("use", "USE", TouchControlAction.USE_ITEM, 0.74f, 0.52f, 0.10f, 0.14f, "circle"));
        list.add(control("camera", "CAMERA", TouchControlAction.CAMERA, 0.48f, 0.28f, 0.34f, 0.34f, "rounded"));
        return list;
    }

    private TouchControlConfig control(String id, String label, TouchControlAction action,
                                       float x, float y, float w, float h, String shape) {
        return new TouchControlConfig(id, label, action, x, y, w, h, 0.82f, shape, true);
    }
}
