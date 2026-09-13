package com.droidlauncher.launcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Builds the complete downloadable installation set from a Minecraft version JSON and asset index. */
public final class FullInstallationPlanner {
    private final MinecraftLibraryMetadataParser libraryParser = new MinecraftLibraryMetadataParser();

    public List<DownloadTask> plan(String versionJson, String assetIndexJson, File minecraftRoot) throws IOException {
        if (versionJson == null || versionJson.trim().isEmpty()) throw new IOException("version JSON is empty");
        if (minecraftRoot == null) throw new IOException("minecraft root is required");

        List<DownloadTask> tasks = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(versionJson);
            String id = root.optString("id", "").trim();
            if (id.isEmpty()) throw new IOException("Minecraft version id is missing");
            File versionDir = VersionInstallation.versionDirectory(minecraftRoot, id);

            JSONObject downloads = root.optJSONObject("downloads");
            JSONObject client = downloads == null ? null : downloads.optJSONObject("client");
            addTask(tasks, "client-" + id,
                    client == null ? "" : client.optString("url", ""),
                    client == null ? "" : client.optString("sha1", ""),
                    new File(versionDir, id + ".jar"));

            for (LibraryDependency dependency : libraryParser.parse(versionJson, minecraftRoot)) {
                addTask(tasks, "library-" + dependency.getCoordinate(), dependency.getUrl(),
                        dependency.getSha1(), dependency.getFile());
            }

            if (assetIndexJson != null && !assetIndexJson.trim().isEmpty()) {
                JSONObject assets = new JSONObject(assetIndexJson);
                JSONObject objects = assets.optJSONObject("objects");
                if (objects != null) {
                    File assetObjects = new File(VersionInstallation.assetsRoot(minecraftRoot), "objects");
                    JSONArray names = objects.names();
                    if (names != null) {
                        for (int i = 0; i < names.length(); i++) {
                            String name = names.optString(i, "");
                            JSONObject object = objects.optJSONObject(name);
                            if (object == null) continue;
                            String hash = object.optString("hash", "");
                            long size = object.optLong("size", -1L);
                            AssetObject asset = new AssetObject(name, hash, size);
                            if (asset.getHash().length() < 2) throw new IOException("Invalid asset hash: " + name);
                            File destination = new File(new File(assetObjects, asset.getHash().substring(0, 2)), asset.getHash());
                            URL url = new URL("https://resources.download.minecraft.net/"
                                    + asset.getHash().substring(0, 2) + "/" + asset.getHash());
                            addTask(tasks, "asset-" + name, url.toString(), asset.getHash(), destination);
                        }
                    }
                }
            }
            return tasks;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Invalid Minecraft installation metadata", e);
        }
    }

    private void addTask(List<DownloadTask> tasks, String name, String url, String sha1, File destination) throws IOException {
        if (url == null || url.trim().isEmpty()) throw new IOException("Missing download URL for " + name);
        if (sha1 == null || sha1.trim().isEmpty()) throw new IOException("Missing SHA-1 for " + name);
        tasks.add(new DownloadTask(name, new URL(url), destination, sha1));
    }
}
