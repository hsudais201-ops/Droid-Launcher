package com.droidlauncher.launcher;

import java.util.Objects;

/** Immutable metadata for one Minecraft client version. */
public final class MinecraftVersion {
    private final String id;
    private final String type;
    private final String url;
    private final String sha1;

    public MinecraftVersion(String id, String type, String url, String sha1) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("version id is required");
        if (url == null || url.trim().isEmpty()) throw new IllegalArgumentException("version url is required");
        this.id = id;
        this.type = type == null ? "release" : type;
        this.url = url;
        this.sha1 = sha1 == null ? "" : sha1;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getUrl() { return url; }
    public String getSha1() { return sha1; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinecraftVersion)) return false;
        MinecraftVersion that = (MinecraftVersion) o;
        return id.equals(that.id) && url.equals(that.url) && sha1.equals(that.sha1);
    }

    @Override public int hashCode() { return Objects.hash(id, url, sha1); }
}
