package com.droidlauncher.launcher;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Minimal dependency-free Microsoft public-client device-code OAuth client. */
public final class MicrosoftOAuthClient {
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 30000;

    public MicrosoftDeviceCode requestDeviceCode(MicrosoftAuthConfig config) throws IOException {
        return requestDeviceCode(config, config.getScope());
    }

    public MicrosoftDeviceCode requestDeviceCode(MicrosoftAuthConfig config, String scope) throws IOException {
        if (config == null) throw new IllegalArgumentException("config is required");
        String body = form("client_id", config.getClientId(), "scope", scope);
        JSONObject json = postForm(config.getDeviceCodeEndpoint(), body);
        String verification = json.optString("verification_uri", json.optString("verification_uri_complete", ""));
        String userCode = json.optString("user_code", "");
        String deviceCode = json.optString("device_code", "");
        long expiresIn = json.optLong("expires_in", 900L);
        long interval = json.optLong("interval", 5L) * 1000L;
        if (verification.isEmpty() || userCode.isEmpty() || deviceCode.isEmpty()) {
            throw new IOException("Microsoft device-code response is incomplete");
        }
        return new MicrosoftDeviceCode(userCode, verification, deviceCode,
                System.currentTimeMillis() + expiresIn * 1000L, interval);
    }

    public MicrosoftTokenResponse pollToken(MicrosoftAuthConfig config, MicrosoftDeviceCode deviceCode)
            throws IOException, InterruptedException {
        if (config == null || deviceCode == null) throw new IllegalArgumentException("config and deviceCode are required");
        while (System.currentTimeMillis() < deviceCode.getExpiresAtMillis()) {
            String body = form("grant_type", "urn:ietf:params:oauth:grant-type:device_code",
                    "client_id", config.getClientId(), "device_code", deviceCode.getDeviceCode());
            JSONObject json = postFormAllowOAuthErrors(config.getTokenEndpoint(), body);
            String access = json.optString("access_token", "");
            if (!access.isEmpty()) {
                return new MicrosoftTokenResponse(access, json.optString("refresh_token", ""),
                        json.optLong("expires_in", 3600L));
            }
            String error = json.optString("error", "");
            if (!("authorization_pending".equals(error) || "slow_down".equals(error))) {
                throw new IOException("Microsoft authentication failed: " + error +
                        " " + json.optString("error_description", ""));
            }
            Thread.sleep(deviceCode.getIntervalMillis() + ("slow_down".equals(error) ? 5000L : 0L));
        }
        throw new IOException("Microsoft device-code authentication expired");
    }

    private JSONObject postForm(String endpoint, String body) throws IOException {
        HttpURLConnection connection = open(endpoint);
        try {
            writeBody(connection, body);
            int code = connection.getResponseCode();
            String response = read(connection, code);
            if (code < 200 || code >= 300) throw new IOException("OAuth HTTP " + code + ": " + response);
            return new JSONObject(response);
        } catch (Exception e) {
            if (e instanceof IOException) throw (IOException) e;
            throw new IOException("Invalid OAuth response", e);
        } finally {
            connection.disconnect();
        }
    }

    private JSONObject postFormAllowOAuthErrors(String endpoint, String body) throws IOException {
        HttpURLConnection connection = open(endpoint);
        try {
            writeBody(connection, body);
            int code = connection.getResponseCode();
            String response = read(connection, code);
            try {
                return new JSONObject(response);
            } catch (org.json.JSONException e) {
                throw new IOException("Invalid OAuth response (HTTP " + code + ")", e);
            }
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection open(String endpoint) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    private void writeBody(HttpURLConnection connection, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        try (OutputStream out = connection.getOutputStream()) {
            out.write(bytes);
        }
    }

    private String read(HttpURLConnection connection, int responseCode) throws IOException {
        java.io.InputStream stream = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }

    private String form(String... values) throws IOException {
        if ((values.length & 1) != 0) throw new IOException("Invalid OAuth form");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < values.length; i += 2) {
            if (out.length() > 0) out.append('&');
            out.append(URLEncoder.encode(values[i], "UTF-8"));
            out.append('=');
            out.append(URLEncoder.encode(values[i + 1] == null ? "" : values[i + 1], "UTF-8"));
        }
        return out.toString();
    }

    public static final class MicrosoftTokenResponse {
        private final String accessToken;
        private final String refreshToken;
        private final long expiresInSeconds;

        MicrosoftTokenResponse(String accessToken, String refreshToken, long expiresInSeconds) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresInSeconds = expiresInSeconds;
        }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public long getExpiresInSeconds() { return expiresInSeconds; }
    }
}
