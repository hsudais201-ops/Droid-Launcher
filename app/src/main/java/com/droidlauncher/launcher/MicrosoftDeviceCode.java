package com.droidlauncher.launcher;

/** Data returned by Microsoft's OAuth device-code request. */
public final class MicrosoftDeviceCode {
    private final String userCode;
    private final String verificationUri;
    private final String deviceCode;
    private final long expiresAtMillis;
    private final long intervalMillis;

    public MicrosoftDeviceCode(String userCode, String verificationUri, String deviceCode,
                                long expiresAtMillis, long intervalMillis) {
        if (userCode == null || userCode.trim().isEmpty()) throw new IllegalArgumentException("userCode is required");
        if (verificationUri == null || verificationUri.trim().isEmpty()) throw new IllegalArgumentException("verificationUri is required");
        if (deviceCode == null || deviceCode.trim().isEmpty()) throw new IllegalArgumentException("deviceCode is required");
        this.userCode = userCode.trim();
        this.verificationUri = verificationUri.trim();
        this.deviceCode = deviceCode.trim();
        this.expiresAtMillis = expiresAtMillis;
        this.intervalMillis = Math.max(1000L, intervalMillis);
    }

    public String getUserCode() { return userCode; }
    public String getVerificationUri() { return verificationUri; }
    public String getDeviceCode() { return deviceCode; }
    public long getExpiresAtMillis() { return expiresAtMillis; }
    public long getIntervalMillis() { return intervalMillis; }
}
