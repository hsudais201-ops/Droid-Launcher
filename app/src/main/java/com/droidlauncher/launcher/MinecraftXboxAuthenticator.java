package com.droidlauncher.launcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Exchanges a Microsoft access token for the Xbox Live, XSTS and Minecraft Services session. */
public final class MinecraftXboxAuthenticator {
    private static final String XBOX_USER_AUTH = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MINECRAFT_LOGIN = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MINECRAFT_ENTITLEMENTS = "https://api.minecraftservices.com/entitlements/mcstore";
    private static final String MINECRAFT_PROFILE = "https://api.minecraftservices.com/minecraft/profile";
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 30000;

    /**
     * Performs the complete online authentication chain and verifies that the account can access
     * Minecraft before returning a launchable session.
     */
    public MinecraftAuthSession authenticate(String microsoftAccessToken) throws IOException {
        if (microsoftAccessToken == null || microsoftAccessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Microsoft access token is required");
        }

        JSONObject xbox = postJson(XBOX_USER_AUTH, new JSONObject()
                .put("RelyingParty", "http://auth.xboxlive.com")
                .put("TokenType", "JWT")
                .put("Properties", new JSONObject()
                        .put("AuthMethod", "RPS")
                        .put("SiteName", "user.auth.xboxlive.com")
                        .put("RpsTicket", "d=" + microsoftAccessToken)), null);

        String xboxToken = requireString(xbox, "Token", "Xbox Live token");
        String userHash = extractUserHash(xbox);

        JSONObject xsts = postJson(XSTS_AUTH, new JSONObject()
                .put("RelyingParty", "rp://api.minecraftservices.com/")
                .put("TokenType", "JWT")
                .put("Properties", new JSONObject()
                        .put("SandboxId", "RETAIL")
                        .put("UserTokens", new JSONArray().put(xboxToken))), null);

        String xstsToken = requireString(xsts, "Token", "XSTS token");

        String identityToken = "XBL3.0 x=" + userHash + ";" + xstsToken;
        JSONObject minecraft = postJson(MINECRAFT_LOGIN, new JSONObject()
                .put("identityToken", identityToken)
                .put("ensureLegacyEnabled", true), null);
        String minecraftAccessToken = requireString(minecraft, "access_token", "Minecraft access token");
        long expiresIn = minecraft.optLong("expires_in", 86400L);

        JSONObject entitlements = getJson(MINECRAFT_ENTITLEMENTS, bearer(minecraftAccessToken));
        JSONArray items = entitlements.optJSONArray("items");
        if (items == null || items.length() == 0) {
            throw new IOException("This Microsoft account does not own Minecraft Java Edition");
        }

        JSONObject profile = getJson(MINECRAFT_PROFILE, bearer(minecraftAccessToken));
        String uuid = requireString(profile, "id", "Minecraft profile UUID");
        String name = requireString(profile, "name", "Minecraft profile name");

        long expiresAt = System.currentTimeMillis() + Math.max(60L, expiresIn) * 1000L;
        AuthenticatedProfile authenticatedProfile = new AuthenticatedProfile(
                uuid, name, minecraftAccessToken, uuid, expiresAt);
        return new MinecraftAuthSession(authenticatedProfile, minecraftAccessToken);
    }

    private Map<String, String> bearer(String accessToken) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        return headers;
    }

    private JSONObject postJson(String endpoint, JSONObject body, Map<String, String> extraHeaders) throws IOException {
        HttpURLConnection connection = open(endpoint, "POST");
        try {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-xbl-contract-version", "1");
            applyHeaders(connection, extraHeaders);
            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload);
            }
            return readJsonResponse(connection);
        } finally {
            connection.disconnect();
        }
    }

    private JSONObject getJson(String endpoint, Map<String, String> headers) throws IOException {
        HttpURLConnection connection = open(endpoint, "GET");
        try {
            connection.setRequestProperty("Accept", "application/json");
            applyHeaders(connection, headers);
            return readJsonResponse(connection);
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection open(String endpoint, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestMethod(method);
        connection.setDoInput(true);
        if ("POST".equals(method)) connection.setDoOutput(true);
        return connection;
    }

    private void applyHeaders(HttpURLConnection connection, Map<String, String> headers) {
        if (headers == null) return;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private JSONObject readJsonResponse(HttpURLConnection connection) throws IOException {
        int code = connection.getResponseCode();
        String response = read(connection, code);
        if (code < 200 || code >= 300) {
            String detail = response;
            try {
                JSONObject error = new JSONObject(response);
                detail = error.optString("errorMessage", error.optString("Message", response));
                String xboxCode = error.optString("XErr", "");
                if (!xboxCode.isEmpty()) detail += " (XErr=" + xboxCode + ")";
            } catch (Exception ignored) {
                // Keep the raw response when it is not JSON.
            }
            throw new IOException("Authentication HTTP " + code + ": " + detail);
        }
        try {
            return new JSONObject(response);
        } catch (Exception e) {
            throw new IOException("Authentication returned invalid JSON", e);
        }
    }

    private String extractUserHash(JSONObject xbox) throws IOException {
        JSONObject displayClaims = xbox.optJSONObject("DisplayClaims");
        JSONArray xui = displayClaims == null ? null : displayClaims.optJSONArray("xui");
        JSONObject first = xui == null || xui.length() == 0 ? null : xui.optJSONObject(0);
        return requireString(first, "uhs", "Xbox user hash");
    }

    private String requireString(JSONObject json, String key, String label) throws IOException {
        if (json == null) throw new IOException(label + " response is incomplete");
        String value = json.optString(key, "").trim();
        if (value.isEmpty()) throw new IOException(label + " is missing");
        return value;
    }

    private String read(HttpURLConnection connection, int responseCode) throws IOException {
        InputStream stream = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }
}
