package com.droidlauncher.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Builds the stable command-line arguments expected by modern Minecraft clients. */
public final class MinecraftLaunchArguments {
    public List<String> build(String versionId, String mainClass, String username,
                              String uuid, String accessToken, java.io.File gameDirectory,
                              java.io.File assetsDirectory, String assetIndexId) {
        if (versionId == null || versionId.trim().isEmpty()) throw new IllegalArgumentException("versionId is required");
        if (gameDirectory == null) throw new IllegalArgumentException("gameDirectory is required");
        if (assetsDirectory == null) throw new IllegalArgumentException("assetsDirectory is required");
        String safeName = username == null || username.trim().isEmpty() ? "Player" : username.trim();
        String safeUuid = uuid == null || uuid.trim().isEmpty()
                ? UUID.nameUUIDFromBytes(safeName.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString()
                : uuid.trim();
        String safeToken = accessToken == null ? "0" : accessToken;
        List<String> args = new ArrayList<>();
        args.add("--username");
        args.add(safeName);
        args.add("--version");
        args.add(versionId.trim());
        args.add("--gameDir");
        args.add(gameDirectory.getAbsolutePath());
        args.add("--assetsDir");
        args.add(assetsDirectory.getAbsolutePath());
        if (assetIndexId != null && !assetIndexId.trim().isEmpty()) {
            args.add("--assetIndex");
            args.add(assetIndexId.trim());
        }
        args.add("--uuid");
        args.add(safeUuid);
        args.add("--accessToken");
        args.add(safeToken);
        args.add("--userType");
        args.add("legacy");
        args.add("--versionType");
        args.add("release");
        return args;
    }
}
