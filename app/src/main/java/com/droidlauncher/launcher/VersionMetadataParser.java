package com.droidlauncher.launcher;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/** Parses the Minecraft version JSON client and asset index metadata. */
public final class VersionMetadataParser {
    public VersionMetadata parse(String json) throws IOException {
        try {
            JSONObject root = new JSONObject(json);
            String id = root.optString("id", "").trim();
            String mainClass = root.optString("mainClass", "").trim();
            String type = root.optString("type", "").trim();

            JSONObject downloads = root.optJSONObject("downloads");
            JSONObject client = downloads == null ? null : downloads.optJSONObject("client");
            URL clientUrl = url(client == null ? "" : client.optString("url", ""));
            String clientSha1 = client == null ? "" : client.optString("sha1", "");

            JSONObject assetIndex = root.optJSONObject("assetIndex");
            String assetsId = assetIndex == null ? "" : assetIndex.optString("id", "");
            URL assetsUrl = url(assetIndex == null ? "" : assetIndex.optString("url", ""));
            String assetsSha1 = assetIndex == null ? "" : assetIndex.optString("sha1", "");

            String baseUrl = "https://libraries.minecraft.net/";
            JSONObject libraryRepository = root.optJSONObject("libraryRepository");
            if (libraryRepository != null) {
                baseUrl = libraryRepository.optString("url", baseUrl);
            }
            return new VersionMetadata(id, mainClass, type, clientUrl, clientSha1,
                    baseUrl, assetsId, assetsUrl, assetsSha1);
        } catch (MalformedURLException e) {
            throw new IOException("Invalid URL in Minecraft version metadata", e);
        } catch (Exception e) {
            throw new IOException("Invalid Minecraft version metadata", e);
        }
    }

    private URL url(String value) throws MalformedURLException {
        if (value == null || value.trim().isEmpty()) return null;
        return new URL(value.trim());
    }
}
