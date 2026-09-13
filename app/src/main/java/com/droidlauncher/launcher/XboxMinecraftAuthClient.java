package com.droidlauncher.launcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Exchanges a Microsoft access token through Xbox Live, XSTS, and Minecraft Services. */
public final class XboxMinecraftAuthClient {
    private static final String XBL_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    public MinecraftAuthSession exchange(String microsoftAccessToken) throws IOException {
        if (microsoftAccessToken == null || microsoftAccessToken.trim().isEmpty()) {
            throw new IOException("Microsoft access token is required");
        }
        XblToken xbl = authenticateXbox(microsoftAccessToken.trim());
        XstsToken xsts = authorizeXsts(xbl.token);
        String identityToken = "XBL3.0 x=" + xbl.userHash + ";" + xsts.token;
        String minecraftToken = minecraftLogin(identityToken);
        JSONObject profile = getMinecraftProfile(minecraftToken);
        String id = profile.optString("id", "").trim();
        String name = profile.optString("name", "").trim();
        if (id.isEmpty() || name.isEmpty()) throw new IOException("Minecraft profile is unavailable");
        return new MinecraftAuthSession(new AuthenticatedProfile(id, name), minecraftToken);
    }

    private XblToken authenticateXbox(String accessToken) throws IOException {
        JSONObject properties = new JSONObject();
        properties.put("AuthMethod", "RPS");
        properties.put("SiteName", "user.auth.xboxlive.com");
        properties.put("RpsTicket", "d=" + accessToken);
        JSONObject request = new JSONObject();
        request.put("RelyingParty", "http://auth.xboxlive.com");
        request.put("TokenType", "JWT");
        request.put("Properties", properties);
        JSONObject response = postJson(XBL_URL, request.toString());
        String token = response.optString("Token", "").trim();
        String hash = "";
        JSONObject claims = response.optJSONObject("DisplayClaims");
        JSONArray xui = claims == null ? null : claims.optJSONArray("xui");
        if (xui != null && xui.length() > 0) {
            JSONObject first = xui.optJSONObject(0);
            if (first != null) hash = first.optString("uhs", "").trim();
        }
        if (token.isEmpty() || hash.isEmpty()) throw new IOException("Xbox Live authentication response is incomplete");
        return new XblToken(token, hash);
    }

    private XstsToken authorizeXsts(String userToken) throws IOException {
        JSONObject properties = new JSONObject();
        properties.put("SandboxId", "RETAIL");
        JSONArray tokens = new JSONArray();
        tokens.put(userToken);
        properties.put("UserTokens", tokens);
        JSONObject request = new JSONObject();
        request.put("Properties", properties);
        request.put("RelyingParty", "rp://api.minecraftservices.com/");
        request.put("TokenType", "JWT");
        JSONObject response = postJson(XSTS_URL, request.toString());
        String token = response.optString("Token", "").trim();
        if (token.isEmpty()) throw new IOException("XSTS authorization response is incomplete");
        return new XstsToken(token);
    }

    private String minecraftLogin(String identityToken) throws IOException {
        JSONObject request = new JSONObject();
        request.put("identityToken", identityToken);
        JSONObject response = postJson(MC_LOGIN_URL, request.toString());
        String token = response.optString("access_token", "").trim();
        if (token.isEmpty()) throw new IOException("Minecraft Services did not return an access token");
        return token;
    }

    private JSONObject getMinecraftProfile(String token) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(MC_PROFILE_URL).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Accept", "application/json");
        int code = connection.getResponseCode();
        java.io.InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) { connection.disconnect(); throw new IOException("Empty Minecraft profile response"); }
        String body = read(stream);
        connection.disconnect();
        if (code != HttpURLConnection.HTTP_OK) throw new IOException("Minecraft profile HTTP " + code + ": " + body);
        try { return new JSONObject(body); } catch (Exception e) { throw new IOException("Invalid Minecraft profile response", e); }
    }

    private JSONObject postJson(String endpoint, String body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("x-xbl-contract-version", "1");
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(data.length);
        try (OutputStream out = connection.getOutputStream()) { out.write(data); }
        int code = connection.getResponseCode();
        java.io.InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) { connection.disconnect(); throw new IOException("Empty authentication response HTTP " + code); }
        String responseBody = read(stream);
        connection.disconnect();
        if (code < 200 || code >= 300) throw new IOException("Authentication endpoint HTTP " + code + ": " + responseBody);
        try { return new JSONObject(responseBody); } catch (Exception e) { throw new IOException("Invalid authentication response", e); }
    }

    private static String read(java.io.InputStream stream) throws IOException {
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line; while ((line = reader.readLine()) != null) out.append(line);
        }
        return out.toString();
    }

    private static final class XblToken { final String token, userHash; XblToken(String t, String h) { token=t; userHash=h; } }
    private static final class XstsToken { final String token; XstsToken(String t) { token=t; } }
}
