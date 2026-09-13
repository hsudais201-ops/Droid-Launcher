package com.droidlauncher.launcher;

/** One Minecraft asset object identified by its SHA-1 hash. */
public final class AssetObject {
    private final String name;
    private final String hash;
    private final long size;

    public AssetObject(String name, String hash, long size) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("name is required");
        if (hash == null || hash.trim().isEmpty()) throw new IllegalArgumentException("hash is required");
        this.name = name;
        this.hash = hash.toLowerCase();
        this.size = size;
    }

    public String getName() { return name; }
    public String getHash() { return hash; }
    public long getSize() { return size; }
}
