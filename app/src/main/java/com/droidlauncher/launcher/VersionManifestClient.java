package com.droidlauncher.launcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Fetches and parses the Mojang version manifest without committing network work to the UI thread. */
public final class VersionManifestClient {
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 30000;

    public String fetch(URL manifestUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) manifestUrl.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("Accept", "application/json");
        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("Version manifest HTTP " + code);
            }
            byte[] data = connection.getInputStream().readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        } finally {
            connection.disconnect();
        }
    }

    public List<VersionEntry> parse(String json) throws IOException {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray versions = root.optJSONArray("versions");
            List<VersionEntry> result = new ArrayList<>();
            if (versions == null) return result;
            for (int i = 0; i < versions.length(); i++) {
                JSONObject item = versions.optJSONObject(i);
                if (item == null) continue;
                String id = item.optString("id", "").trim();
                String type = item.optString("type", "").trim();
                String url = item.optString("url", "").trim();
                if (id.isEmpty() || url.isEmpty()) continue;
                result.add(new VersionEntry(id, type, url));
            }
            return result;
        } catch (Exception e) {
            throw new IOException("Invalid Minecraft version manifest", e);
        }
    }

    public static final class VersionEntry {
        private final String id;
        private final String type;
        private final String url;

        public VersionEntry(String id, String type, String url) {
            this.id = id;
            this.type = type;
            this.url = url;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getUrl() { return url; }
    }
}
