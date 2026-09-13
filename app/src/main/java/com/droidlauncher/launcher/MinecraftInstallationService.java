package com.droidlauncher.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Resolves a Minecraft version from Mojang's manifest and builds its complete download plan. */
public final class MinecraftInstallationService {
    public static final String DEFAULT_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    private final VersionManifestClient manifestClient;
    private final VersionMetadataParser metadataParser;
    private final FullInstallationPlanner planner;

    public MinecraftInstallationService() {
        this(new VersionManifestClient(), new VersionMetadataParser(), new FullInstallationPlanner());
    }

    public MinecraftInstallationService(VersionManifestClient manifestClient,
                                        VersionMetadataParser metadataParser,
                                        FullInstallationPlanner planner) {
        this.manifestClient = manifestClient;
        this.metadataParser = metadataParser;
        this.planner = planner;
    }

    public InstallationPlan prepare(String versionId, File minecraftRoot) throws IOException {
        if (versionId == null || versionId.trim().isEmpty()) throw new IOException("version id is required");
        if (minecraftRoot == null) throw new IOException("minecraft root is required");

        String manifestJson = manifestClient.fetch(new URL(DEFAULT_MANIFEST_URL));
        List<VersionManifestClient.VersionEntry> versions = manifestClient.parse(manifestJson);
        VersionManifestClient.VersionEntry selected = null;
        for (VersionManifestClient.VersionEntry entry : versions) {
            if (versionId.equals(entry.getId())) {
                selected = entry;
                break;
            }
        }
        if (selected == null) throw new IOException("Minecraft version not found: " + versionId);

        String versionJson = manifestClient.fetch(new URL(selected.getUrl()));
        VersionMetadata metadata = metadataParser.parse(versionJson);
        if (!versionId.equals(metadata.getId())) {
            throw new IOException("Version metadata id mismatch: expected " + versionId + ", got " + metadata.getId());
        }

        String assetIndexJson = "";
        if (metadata.getAssetsUrl() != null) {
            assetIndexJson = manifestClient.fetch(metadata.getAssetsUrl());
            if (metadata.getAssetsSha1() != null && !metadata.getAssetsSha1().trim().isEmpty()) {
                File temp = new File(VersionInstallation.assetsRoot(minecraftRoot), ".index-" + versionId + ".json.tmp");
                writeUtf8(temp, assetIndexJson);
                if (!Sha1Verifier.verify(temp, metadata.getAssetsSha1())) {
                    temp.delete();
                    throw new IOException("Asset index SHA-1 mismatch for " + versionId);
                }
                temp.delete();
            }
        }

        List<DownloadTask> tasks = planner.plan(versionJson, assetIndexJson, minecraftRoot);
        return new InstallationPlan(metadata, versionJson, assetIndexJson, tasks);
    }

    private static void writeUtf8(File file, String value) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Cannot create directory: " + parent);
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(value.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static final class InstallationPlan {
        private final VersionMetadata metadata;
        private final String versionJson;
        private final String assetIndexJson;
        private final List<DownloadTask> tasks;

        public InstallationPlan(VersionMetadata metadata, String versionJson,
                                String assetIndexJson, List<DownloadTask> tasks) {
            this.metadata = metadata;
            this.versionJson = versionJson;
            this.assetIndexJson = assetIndexJson;
            this.tasks = tasks;
        }

        public VersionMetadata getMetadata() { return metadata; }
        public String getVersionJson() { return versionJson; }
        public String getAssetIndexJson() { return assetIndexJson; }
        public List<DownloadTask> getTasks() { return tasks; }
    }
}
