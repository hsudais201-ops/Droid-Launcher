package com.droidlauncher.launcher;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Converts parsed Minecraft metadata into concrete download tasks. */
public final class VersionDownloadPlanner {
    public List<DownloadTask> plan(VersionMetadata metadata, File minecraftRoot) {
        if (metadata == null) throw new IllegalArgumentException("metadata is required");
        if (minecraftRoot == null) throw new IllegalArgumentException("minecraftRoot is required");

        List<DownloadTask> tasks = new ArrayList<>();
        File versionDir = VersionInstallation.versionDirectory(minecraftRoot, metadata.getId());

        if (metadata.getClientUrl() != null) {
            tasks.add(task("client-" + metadata.getId(), metadata.getClientUrl(),
                    metadata.getClientSha1(), new File(versionDir, metadata.getId() + ".jar")));
        }

        if (metadata.getAssetsIndexUrl() != null && !metadata.getAssetsIndexId().isEmpty()) {
            File indexes = new File(VersionInstallation.assetsRoot(minecraftRoot), "indexes");
            tasks.add(task("asset-index-" + metadata.getAssetsIndexId(),
                    metadata.getAssetsIndexUrl(), metadata.getAssetsIndexSha1(),
                    new File(indexes, metadata.getAssetsIndexId() + ".json")));
        }
        return tasks;
    }

    private DownloadTask task(String name, URL url, String sha1, File destination) {
        if (sha1 == null || sha1.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing SHA-1 for " + name);
        }
        return new DownloadTask(name, url, sha1, destination);
    }
}
