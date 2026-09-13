package com.droidlauncher.launcher;

import java.io.File;

/** Resolves the canonical local locations for a Minecraft version installation. */
public final class VersionInstallation {
    private VersionInstallation() { }

    public static File versionDirectory(File minecraftRoot, String versionId) {
        requireRoot(minecraftRoot);
        requireId(versionId);
        return new File(new File(minecraftRoot, "versions"), versionId);
    }

    public static File clientJar(File minecraftRoot, String versionId) {
        return new File(versionDirectory(minecraftRoot, versionId), versionId + ".jar");
    }

    public static File versionJson(File minecraftRoot, String versionId) {
        return new File(versionDirectory(minecraftRoot, versionId), versionId + ".json");
    }

    public static File librariesRoot(File minecraftRoot) {
        requireRoot(minecraftRoot);
        return new File(minecraftRoot, "libraries");
    }

    public static File assetsRoot(File minecraftRoot) {
        requireRoot(minecraftRoot);
        return new File(minecraftRoot, "assets");
    }

    private static void requireRoot(File root) {
        if (root == null) throw new IllegalArgumentException("minecraftRoot is required");
    }

    private static void requireId(String id) {
        if (id == null || id.trim().isEmpty() || id.contains("/") || id.contains("\\") || id.contains("..")) {
            throw new IllegalArgumentException("invalid version id");
        }
    }
}
