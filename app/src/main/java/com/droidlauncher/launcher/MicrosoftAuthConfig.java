package com.droidlauncher.launcher;

/** Configuration for the public Microsoft identity-platform client used by Droid Launcher. */
public final class MicrosoftAuthConfig {
    public static final String AUTHORITY = "https://login.microsoftonline.com/consumers/oauth2/v2.0";
    public static final String DEVICE_CODE_ENDPOINT = AUTHORITY + "/devicecode";
    public static final String TOKEN_ENDPOINT = AUTHORITY + "/token";
    public static final String DEFAULT_SCOPE = "XboxLive.signin XboxLive.offline_access";

    private final String clientId;
    private final String scope;

    public MicrosoftAuthConfig(String clientId) {
        this(clientId, DEFAULT_SCOPE);
    }

    public MicrosoftAuthConfig(String clientId, String scope) {
        if (clientId == null || clientId.trim().isEmpty()) throw new IllegalArgumentException("Microsoft client id is required");
        if (scope == null || scope.trim().isEmpty()) throw new IllegalArgumentException("Microsoft scope is required");
        this.clientId = clientId.trim();
        this.scope = scope.trim();
    }

    public String getClientId() { return clientId; }
    public String getScope() { return scope; }
}
