package com.droidlauncher.launcher;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Exchanges a stored Microsoft refresh token for a fresh Microsoft access token. */
public final class MicrosoftTokenRefresher {
    public MicrosoftOAuthClient.MicrosoftTokenResponse refresh(MicrosoftAuthConfig config, String refreshToken)
            throws IOException {
        if (config == null) throw new IllegalArgumentException("config is required");
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("refresh token is required");
        }
        String body = form(
                "client_id", config.getClientId(),
                "grant_type", "refresh_token",
                "refresh_token", refreshToken.trim(),
                "scope", config.getScope());
        HttpURLConnection connection = (HttpURLConnection) new URL(config.getTokenEndpoint()).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        try {
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload);
            }
            int code = connection.getResponseCode();
            String response = read(connection, code);
            JSONObject json;
            try {
                json = new JSONObject(response);
            } catch (Exception e) {
                throw new IOException("Microsoft refresh response is not valid JSON (HTTP " + code + ")", e);
            }
            if (code < 200 || code >= 300) {
                throw new IOException("Microsoft refresh failed: "
                        + json.optString("error", "HTTP " + code) + " "
                        + json.optString("error_description", ""));
            }
            String accessToken = json.optString("access_token", "").trim();
            if (accessToken.isEmpty()) throw new IOException("Microsoft refresh response has no access token");
            String replacementRefreshToken = json.optString("refresh_token", "").trim();
            if (replacementRefreshToken.isEmpty()) replacementRefreshToken = refreshToken.trim();
            return new MicrosoftOAuthClient.MicrosoftTokenResponse(
                    accessToken, replacementRefreshToken, json.optLong("expires_in", 3600L));
        } finally {
            connection.disconnect();
        }
    }

    private String form(String... values) throws IOException {
        if ((values.length & 1) != 0) throw new IOException("Invalid Microsoft refresh form");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < values.length; i += 2) {
            if (out.length() > 0) out.append('&');
            out.append(URLEncoder.encode(values[i], "UTF-8"));
            out.append('=');
            out.append(URLEncoder.encode(values[i + 1] == null ? "" : values[i + 1], "UTF-8"));
        }
        return out.toString();
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
