package com.droidlauncher;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Persistent named touch-control profiles and built-in presets. */
public final class TouchControlProfileStore {
    private static final String PREFS = "touch_control_profiles";
    private static final String KEY_PROFILES = "profiles";
    private final SharedPreferences preferences;

    public TouchControlProfileStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<String> listProfiles() {
        List<String> result = new ArrayList<>();
        JSONArray array = readRoot().optJSONArray("profiles");
        if (array != null) for (int i = 0; i < array.length(); i++) {
            String name = array.optString(i, "").trim();
            if (!name.isEmpty()) result.add(name);
        }
        if (result.isEmpty()) result.add("Custom");
        return result;
    }

    public void saveProfile(String name, List<TouchControlConfig> controls) {
        String normalized = normalize(name);
        if (normalized.isEmpty()) return;
        try {
            JSONObject root = readRoot();
            JSONObject layouts = root.optJSONObject("layouts");
            if (layouts == null) { layouts = new JSONObject(); root.put("layouts", layouts); }
            JSONArray values = new JSONArray();
            for (TouchControlConfig c : controls) {
                JSONObject o = new JSONObject();
                o.put("id", c.id); o.put("label", c.label); o.put("action", c.action.name());
                o.put("x", c.x); o.put("y", c.y); o.put("width", c.width); o.put("height", c.height);
                o.put("opacity", c.opacity); o.put("shape", c.shape); o.put("visible", c.visible);
                values.put(o);
            }
            layouts.put(normalized, values);
            JSONArray names = root.getJSONArray("profiles");
            boolean exists = false;
            for (int i = 0; i < names.length(); i++) {
                if (normalized.equalsIgnoreCase(names.optString(i))) { exists = true; break; }
            }
            if (!exists) names.put(normalized);
            writeRoot(root);
        } catch (Exception ignored) { }
    }

    public List<TouchControlConfig> loadProfile(String name) {
        try {
            JSONObject layouts = readRoot().optJSONObject("layouts");
            if (layouts == null) return null;
            JSONArray values = layouts.optJSONArray(normalize(name));
            if (values == null || values.length() == 0) return null;
            List<TouchControlConfig> result = new ArrayList<>();
            for (int i = 0; i < values.length(); i++) {
                JSONObject o = values.getJSONObject(i);
                TouchControlAction action;
                try { action = TouchControlAction.valueOf(o.optString("action", "JUMP")); }
                catch (IllegalArgumentException e) { action = TouchControlAction.JUMP; }
                result.add(new TouchControlConfig(
                        o.optString("id"), o.optString("label", action.getLabel()), action,
                        (float)o.optDouble("x", 0.5), (float)o.optDouble("y", 0.5),
                        (float)o.optDouble("width", 0.10), (float)o.optDouble("height", 0.15),
                        (float)o.optDouble("opacity", 0.82), o.optString("shape", "rounded"),
                        o.optBoolean("visible", true)));
            }
            return result;
        } catch (Exception e) { return null; }
    }

    public void deleteProfile(String name) {
        String normalized = normalize(name);
        if (normalized.isEmpty() || isBuiltIn(normalized)) return;
        try {
            JSONObject root = readRoot();
            JSONArray names = root.getJSONArray("profiles");
            for (int i = names.length() - 1; i >= 0; i--) {
                if (normalized.equalsIgnoreCase(names.optString(i))) names.remove(i);
            }
            JSONObject layouts = root.optJSONObject("layouts");
            if (layouts != null) layouts.remove(normalized);
            writeRoot(root);
        } catch (Exception ignored) { }
    }

    public List<TouchControlConfig> preset(String name) {
        if ("Survival".equalsIgnoreCase(name)) return survival();
        if ("PvP".equalsIgnoreCase(name)) return pvp();
        if ("Building".equalsIgnoreCase(name)) return building();
        if ("Parkour".equalsIgnoreCase(name)) return parkour();
        if ("Creative".equalsIgnoreCase(name)) return creative();
        if ("Controller".equalsIgnoreCase(name)) return controller();
        return null;
    }

    private JSONObject readRoot() {
        try {
            String raw = preferences.getString(KEY_PROFILES, "");
            if (!raw.trim().isEmpty()) {
                JSONObject root = new JSONObject(raw);
                if (root.optJSONArray("profiles") == null) root.put("profiles", new JSONArray());
                return root;
            }
        } catch (Exception ignored) { }
        JSONObject root = new JSONObject();
        try { root.put("profiles", new JSONArray()); root.put("layouts", new JSONObject()); }
        catch (Exception ignored) { }
        return root;
    }

    private void writeRoot(JSONObject root) { preferences.edit().putString(KEY_PROFILES, root.toString()).apply(); }

    private String normalize(String value) {
        if (value == null) return "";
        String clean = value.trim().replace("/", "_").replace("\\", "_");
        return clean.length() > 32 ? clean.substring(0, 32) : clean;
    }

    private boolean isBuiltIn(String value) {
        return "Custom".equalsIgnoreCase(value) || "Survival".equalsIgnoreCase(value)
                || "PvP".equalsIgnoreCase(value) || "Building".equalsIgnoreCase(value)
                || "Parkour".equalsIgnoreCase(value) || "Creative".equalsIgnoreCase(value)
                || "Controller".equalsIgnoreCase(value);
    }

    private TouchControlConfig c(String id, String label, TouchControlAction action,
                                 float x, float y, float w, float h, String shape) {
        return new TouchControlConfig(id, label, action, x, y, w, h, 0.82f, shape, true);
    }

    private List<TouchControlConfig> base(boolean controller) {
        List<TouchControlConfig> list = new ArrayList<>();
        list.add(c("move", "MOVE", TouchControlAction.JOYSTICK, 0.03f, 0.65f, 0.19f, 0.27f, "circle"));
        list.add(c("camera", "CAMERA", TouchControlAction.CAMERA, 0.42f, 0.20f, 0.34f, 0.43f, "rounded"));
        list.add(c("jump", "JUMP", TouchControlAction.JUMP, 0.84f, 0.63f, 0.10f, 0.16f, "circle"));
        list.add(c("attack", "ATTACK", TouchControlAction.ATTACK, 0.84f, 0.42f, 0.10f, 0.16f, "circle"));
        list.add(c("use", "USE", TouchControlAction.USE_ITEM, 0.72f, 0.50f, 0.10f, 0.14f, "circle"));
        if (controller) {
            list.add(c("inventory", "INV", TouchControlAction.INVENTORY, 0.29f, 0.84f, 0.10f, 0.10f, "rounded"));
            list.add(c("drop", "DROP", TouchControlAction.DROP, 0.42f, 0.84f, 0.10f, 0.10f, "rounded"));
            list.add(c("pause", "PAUSE", TouchControlAction.PAUSE, 0.55f, 0.84f, 0.10f, 0.10f, "rounded"));
        }
        return list;
    }

    private List<TouchControlConfig> survival() { return base(false); }

    private List<TouchControlConfig> pvp() {
        List<TouchControlConfig> list = base(false);
        list.get(2).x = 0.82f; list.get(2).y = 0.50f;
        list.get(3).x = 0.88f; list.get(3).y = 0.34f;
        list.add(c("sprint", "SPRINT", TouchControlAction.SPRINT, 0.24f, 0.78f, 0.10f, 0.12f, "rounded"));
        list.add(c("sneak", "SNEAK", TouchControlAction.SNEAK, 0.34f, 0.78f, 0.10f, 0.12f, "rounded"));
        return list;
    }

    private List<TouchControlConfig> building() {
        List<TouchControlConfig> list = base(false);
        list.add(c("sneak", "SNEAK", TouchControlAction.SNEAK, 0.28f, 0.78f, 0.10f, 0.12f, "rounded"));
        list.add(c("drop", "DROP", TouchControlAction.DROP, 0.40f, 0.78f, 0.10f, 0.12f, "rounded"));
        return list;
    }

    private List<TouchControlConfig> parkour() {
        List<TouchControlConfig> list = base(false);
        list.get(2).x = 0.78f; list.get(2).y = 0.56f;
        list.add(c("sprint", "SPRINT", TouchControlAction.SPRINT, 0.26f, 0.76f, 0.11f, 0.13f, "rounded"));
        return list;
    }

    private List<TouchControlConfig> creative() {
        List<TouchControlConfig> list = base(false);
        list.add(c("inventory", "INV", TouchControlAction.INVENTORY, 0.30f, 0.84f, 0.10f, 0.10f, "rounded"));
        list.add(c("chat", "CHAT", TouchControlAction.CHAT, 0.42f, 0.84f, 0.10f, 0.10f, "rounded"));
        list.add(c("drop", "DROP", TouchControlAction.DROP, 0.54f, 0.84f, 0.10f, 0.10f, "rounded"));
        return list;
    }

    private List<TouchControlConfig> controller() { return base(true); }
}
