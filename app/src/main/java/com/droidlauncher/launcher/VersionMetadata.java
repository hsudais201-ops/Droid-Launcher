package com.droidlauncher.launcher;

import java.net.URL;

/** Minimal Minecraft version metadata needed to prepare a launch installation. */
public final class VersionMetadata {
    private final String id;
    private final String mainClass;
    private final String type;
    private final URL clientUrl;
    private final String clientSha1;
    private final String librariesBaseUrl;
    private final String assetsIndexId;
    private final URL assetsIndexUrl;
    private final String assetsIndexSha1;

    public VersionMetadata(String id, String mainClass, String type,
                           URL clientUrl, String clientSha1,
                           String librariesBaseUrl, String assetsIndexId,
                           URL assetsIndexUrl, String assetsIndexSha1) {
        this.id = id;
        this.mainClass = mainClass == null ? "" : mainClass;
        this.type = type == null ? "" : type;
        this.clientUrl = clientUrl;
        this.clientSha1 = clientSha1 == null ? "" : clientSha1;
        this.librariesBaseUrl = librariesBaseUrl == null ? "" : librariesBaseUrl;
        this.assetsIndexId = assetsIndexId == null ? "" : assetsIndexId;
        this.assetsIndexUrl = assetsIndexUrl;
        this.assetsIndexSha1 = assetsIndexSha1 == null ? "" : assetsIndexSha1;
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("id is required");
    }

    public String getId() { return id; }
    public String getMainClass() { return mainClass; }
    public String getType() { return type; }
    public URL getClientUrl() { return clientUrl; }
    public String getClientSha1() { return clientSha1; }
    public String getLibrariesBaseUrl() { return librariesBaseUrl; }
    public String getAssetsIndexId() { return assetsIndexId; }
    public URL getAssetsIndexUrl() { return assetsIndexUrl; }
    public String getAssetsIndexSha1() { return assetsIndexSha1; }

    /** Compatibility aliases retained for older installation code. */
    public URL getAssetsUrl() { return assetsIndexUrl; }
    public String getAssetsSha1() { return assetsIndexSha1; }
}
