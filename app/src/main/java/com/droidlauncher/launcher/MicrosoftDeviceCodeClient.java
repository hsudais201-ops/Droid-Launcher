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

/** Performs the Microsoft public-client device-code flow without embedding a client secret. */
public final class MicrosoftDeviceCodeClient {
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 30000;

    public DeviceCode requestDeviceCode(MicrosoftAuthConfig config) throws IOException {
        String body = form("client_id", config.getClientId(), "scope", config.getScope());
        JSONObject json = postForm(MicrosoftAuthConfig.DEVICE_CODE_ENDPOINT, body);
        String userCode = json.optString("user_code", "").trim();
        String verificationUri = json.optString("verification_uri", "").trim();
        if (verificationUri.isEmpty()) verificationUri = json.optString("verification_uri_complete", "").trim();
        long expiresIn = json.optLong("expires_in", 900L);
        long interval = json.optLong("interval", 5L);
        String deviceCode = json.optString("device_code", "").trim();
        if (userCode.isEmpty() || verificationUri.isEmpty() || deviceCode.isEmpty()) {
            throw new IOException("Microsoft device-code response is incomplete");
        }
        return new DeviceCode(deviceCode, userCode, verificationUri, expiresIn, interval);
    }

    public OAuthTokens pollForToken(MicrosoftAuthConfig config, DeviceCode deviceCode,
                                    Cancellation cancellation) throws IOException {
        long deadline = System.currentTimeMillis() + deviceCode.getExpiresInSeconds() * 1000L;
        long intervalMs = Math.max(1000L, deviceCode.getIntervalSeconds() * 1000L);
        while (System.currentTimeMillis() < deadline) {
            if (cancellation != null && cancellation.isCancelled()) throw new IOException("Microsoft authentication cancelled");
            sleep(intervalMs);
            String body = form("grant_type", "urn:ietf:params:oauth:grant-type:device_code",
                    "client_id", config.getClientId(), "device_code", deviceCode.getDeviceCode());
            HttpResult result = postFormRaw(MicrosoftAuthConfig.TOKEN_ENDPOINT, body);
            final JSONObject json;
            try {
                json = new JSONObject(result.body);
            } catch (Exception e) {
                throw new IOException("Invalid Microsoft token response", e);
            }
            if (result.code == HttpURLConnection.HTTP_OK) {
                String access = json.optString("access_token", "").trim();
                String refresh = json.optString("refresh_token", "").trim();
                long expires = json.optLong("expires_in", 0L);
                if (access.isEmpty()) throw new IOException("Microsoft token response has no access token");
                return new OAuthTokens(access, refresh, expires);
            }
            String error = json.optString("error", "").trim();
            if ("authorization_pending".equals(error)) continue;
            if ("slow_down".equals(error)) { intervalMs += 5000L; continue; }
            if ("expired_token".equals(error)) throw new IOException("Microsoft device code expired");
            if ("access_denied".equals(error)) throw new IOException("Microsoft sign-in was denied");
            String description = json.optString("error_description", "").trim();
            throw new IOException("Microsoft authentication failed: " + (description.isEmpty() ? error : description));
        }
        throw new IOException("Microsoft device-code authentication timed out");
    }

    private JSONObject postForm(String endpoint, String body) throws IOException {
        HttpResult result = postFormRaw(endpoint, body);
        if (result.code != HttpURLConnection.HTTP_OK) throw new IOException("Microsoft endpoint HTTP " + result.code);
        try { return new JSONObject(result.body); }
        catch (Exception e) { throw new IOException("Invalid Microsoft response", e); }
    }

    private HttpResult postFormRaw(String endpoint, String body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(data.length);
        try (OutputStream out = connection.getOutputStream()) { out.write(data); }
        int code = connection.getResponseCode();
        java.io.InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) throw new IOException("Empty Microsoft response");
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line; while ((line = reader.readLine()) != null) text.append(line);
        } finally { connection.disconnect(); }
        return new HttpResult(code, text.toString());
    }

    private static String form(String... values) throws IOException {
        if ((values.length & 1) != 0) throw new IOException("Invalid form arguments");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < values.length; i += 2) {
            if (i > 0) out.append('&');
            out.append(URLEncoder.encode(values[i], "UTF-8"));
            out.append('=');
            out.append(URLEncoder.encode(values[i + 1], "UTF-8"));
        }
        return out.toString();
    }

    private static void sleep(long millis) throws IOException {
        try { Thread.sleep(millis); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IOException("Microsoft authentication interrupted", e); }
    }

    public interface Cancellation { boolean isCancelled(); }

    public static final class DeviceCode {
        private final String deviceCode, userCode, verificationUri;
        private final long expiresInSeconds, intervalSeconds;
        DeviceCode(String deviceCode, String userCode, String verificationUri, long expiresInSeconds, long intervalSeconds) {
            this.deviceCode = deviceCode; this.userCode = userCode; this.verificationUri = verificationUri;
            this.expiresInSeconds = expiresInSeconds; this.intervalSeconds = intervalSeconds;
        }
        public String getDeviceCode() { return deviceCode; }
        public String getUserCode() { return userCode; }
        public String getVerificationUri() { return verificationUri; }
        public long getExpiresInSeconds() { return expiresInSeconds; }
        public long getIntervalSeconds() { return intervalSeconds; }
    }

    public static final class OAuthTokens {
        private final String accessToken, refreshToken; private final long expiresInSeconds;
        OAuthTokens(String accessToken, String refreshToken, long expiresInSeconds) { this.accessToken = accessToken; this.refreshToken = refreshToken; this.expiresInSeconds = expiresInSeconds; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public long getExpiresInSeconds() { return expiresInSeconds; }
    }

    private static final class HttpResult {
        final int code; final String body;
        HttpResult(int code, String body) { this.code = code; this.body = body; }
    }
}
