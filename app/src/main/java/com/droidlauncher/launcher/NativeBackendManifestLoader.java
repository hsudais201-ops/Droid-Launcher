package com.droidlauncher.launcher;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

/** Loads and validates the ABI-specific native backend manifest bundled with the launcher. */
public final class NativeBackendManifestLoader {
    private static final String ASSET_NAME = "native-backends.json";

    public NativeBackendRegistry load(Context context) throws IOException {
        if (context == null) throw new IllegalArgumentException("context is required");
        try (InputStream input = context.getAssets().open(ASSET_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            StringBuilder jsonText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) jsonText.append(line).append('\n');
            return parse(jsonText.toString());
        } catch (JSONException e) {
            throw new IOException("Invalid native backend manifest: " + e.getMessage(), e);
        }
    }

    public NativeBackendRegistry parse(String json) throws JSONException, IOException {
        if (json == null || json.trim().isEmpty()) throw new IOException("Native backend manifest is empty");
        JSONObject root = new JSONObject(json);
        if (root.optInt("schemaVersion", -1) != 1) {
            throw new IOException("Unsupported native backend manifest schema");
        }
        String library = root.optString("library", "").trim();
        if (library.isEmpty() || !library.endsWith(".so") || !library.equals(new java.io.File(library).getName())) {
            throw new IOException("Invalid native backend library name");
        }
        JSONObject backends = root.optJSONObject("backends");
        if (backends == null) throw new IOException("Native backend manifest has no backends object");

        EnumMap<NativeAbi, NativeBackendSpec> specs = new EnumMap<>(NativeAbi.class);
        for (NativeAbi abi : NativeAbi.values()) {
            if (abi == NativeAbi.UNKNOWN) continue;
            JSONObject entry = backends.optJSONObject(abi.getAndroidAbi());
            if (entry == null) throw new IOException("Missing manifest entry for " + abi.getAndroidAbi());
            String urlText = entry.optString("url", "").trim();
            String sha1 = entry.optString("sha1", "").trim();
            try {
                specs.put(abi, new NativeBackendSpec(abi, library, new URL(urlText), sha1));
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid manifest entry for " + abi.getAndroidAbi() + ": " + e.getMessage(), e);
            }
        }
        return new NativeBackendRegistry(specs);
    }
}
