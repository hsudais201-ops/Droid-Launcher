package com.droidlauncher.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Resolves a Minecraft version from Mojang's manifest and builds/runs its complete download plan. */
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
        if (manifestClient == null || metadataParser == null || planner == null) {
            throw new IllegalArgumentException("dependencies are required");
        }
        this.manifestClient = manifestClient;
        this.metadataParser = metadataParser;
        this.planner = planner;
    }

    public InstallationPlan prepare(String versionId, File minecraftRoot) throws IOException {
        if (versionId == null || versionId.trim().isEmpty()) throw new IOException("version id is required");
        if (minecraftRoot == null) throw new IOException("minecraft root is required");

        String manifestJson = manifestClient.fetch(new URL(DEFAULT_MANIFEST_URL));
        String requested = versionId.trim();
        if ("latest".equalsIgnoreCase(requested)) {
            try {
                org.json.JSONObject root = new org.json.JSONObject(manifestJson);
                org.json.JSONObject latest = root.optJSONObject("latest");
                requested = latest == null ? requested : latest.optString("release", requested).trim();
            } catch (Exception e) {
                throw new IOException("Invalid Minecraft version manifest", e);
            }
        }

        List<VersionManifestClient.VersionEntry> versions = manifestClient.parse(manifestJson);
        VersionManifestClient.VersionEntry selected = null;
        for (VersionManifestClient.VersionEntry entry : versions) {
            if (requested.equals(entry.getId())) {
                selected = entry;
                break;
            }
        }
        if (selected == null) throw new IOException("Minecraft version not found: " + requested);

        String versionJson = manifestClient.fetch(new URL(selected.getUrl()));
        VersionMetadata metadata = metadataParser.parse(versionJson);
        if (!selected.getId().equals(metadata.getId())) {
            throw new IOException("Version metadata id mismatch: expected " + selected.getId() + ", got " + metadata.getId());
        }

        String assetIndexJson = "";
        if (metadata.getAssetsUrl() != null) {
            assetIndexJson = manifestClient.fetch(metadata.getAssetsUrl());
            if (metadata.getAssetsSha1() != null && !metadata.getAssetsSha1().trim().isEmpty()) {
                File indexFile = new File(new File(VersionInstallation.assetsRoot(minecraftRoot), "indexes"),
                        metadata.getAssetsIndexId() + ".json");
                writeUtf8(indexFile, assetIndexJson);
                if (!Sha1Verifier.verify(indexFile, metadata.getAssetsSha1())) {
                    throw new IOException("Asset index SHA-1 mismatch for " + selected.getId());
                }
            }
        }

        List<DownloadTask> tasks = planner.plan(versionJson, assetIndexJson, minecraftRoot);
        return new InstallationPlan(metadata, versionJson, assetIndexJson, tasks);
    }

    /** Executes a prepared plan using the existing verified download orchestrator. */
    public void install(InstallationPlan plan, MinecraftDownloadOrchestrator orchestrator,
                        DownloadProgressListener listener) throws IOException {
        if (plan == null) throw new IOException("installation plan is required");
        if (orchestrator == null) throw new IOException("download orchestrator is required");
        orchestrator.install(plan.getTasks(), listener);
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
            if (metadata == null || versionJson == null || assetIndexJson == null || tasks == null) {
                throw new IllegalArgumentException("plan fields are required");
            }
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
