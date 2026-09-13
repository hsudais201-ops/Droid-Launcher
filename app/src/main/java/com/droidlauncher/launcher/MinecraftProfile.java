package com.droidlauncher.launcher;

import java.util.Objects;

/** Immutable description of a Minecraft installation/profile. */
public final class MinecraftProfile {
    private final String id;
    private final String version;
    private final String gameDirectory;
    private final String javaExecutable;
    private final int minRamMb;
    private final int maxRamMb;

    public MinecraftProfile(String id, String version, String gameDirectory,
                            String javaExecutable, int minRamMb, int maxRamMb) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("id is required");
        if (version == null || version.trim().isEmpty()) throw new IllegalArgumentException("version is required");
        if (maxRamMb < minRamMb || minRamMb < 256) throw new IllegalArgumentException("invalid RAM range");
        this.id = id;
        this.version = version;
        this.gameDirectory = gameDirectory == null ? "" : gameDirectory;
        this.javaExecutable = javaExecutable == null ? "" : javaExecutable;
        this.minRamMb = minRamMb;
        this.maxRamMb = maxRamMb;
    }

    public String getId() { return id; }
    public String getVersion() { return version; }
    public String getGameDirectory() { return gameDirectory; }
    public String getJavaExecutable() { return javaExecutable; }
    public int getMinRamMb() { return minRamMb; }
    public int getMaxRamMb() { return maxRamMb; }

    public MinecraftProfile withJavaExecutable(String executable) {
        return new MinecraftProfile(id, version, gameDirectory, executable, minRamMb, maxRamMb);
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MinecraftProfile)) return false;
        MinecraftProfile that = (MinecraftProfile) other;
        return minRamMb == that.minRamMb && maxRamMb == that.maxRamMb
                && id.equals(that.id) && version.equals(that.version)
                && gameDirectory.equals(that.gameDirectory)
                && javaExecutable.equals(that.javaExecutable);
    }

    @Override public int hashCode() {
        return Objects.hash(id, version, gameDirectory, javaExecutable, minRamMb, maxRamMb);
    }
}
