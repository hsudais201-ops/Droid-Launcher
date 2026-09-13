package com.droidlauncher.launcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Parses Minecraft library entries into verified JVM and Linux-native artifacts. */
public final class MinecraftLibraryMetadataParser {
    public List<LibraryDependency> parse(String versionJson, File minecraftRoot) throws IOException {
        if (versionJson == null || versionJson.trim().isEmpty()) throw new IOException("version JSON is empty");
        if (minecraftRoot == null) throw new IOException("minecraft root is required");
        try {
            JSONObject root = new JSONObject(versionJson);
            JSONArray libraries = root.optJSONArray("libraries");
            List<LibraryDependency> result = new ArrayList<>();
            if (libraries == null) return result;
            for (int i = 0; i < libraries.length(); i++) {
                JSONObject library = libraries.optJSONObject(i);
                if (library == null || !allowed(library)) continue;
                String coordinate = library.optString("name", "").trim();
                if (coordinate.isEmpty()) continue;

                JSONObject downloads = library.optJSONObject("downloads");
                JSONObject artifact = downloads == null ? null : downloads.optJSONObject("artifact");
                if (artifact != null) add(result, coordinate, artifact, minecraftRoot, false, "", "");

                JSONObject natives = library.optJSONObject("natives");
                if (natives != null && natives.has("linux")) {
                    String classifier = natives.optString("linux", "").trim();
                    JSONObject classifiers = downloads == null ? null : downloads.optJSONObject("classifiers");
                    JSONObject nativeArtifact = classifiers == null ? null : classifiers.optJSONObject(classifier);
                    if (nativeArtifact != null) {
                        String arch = nativeArtifact.optString("path", "");
                        add(result, coordinate + ":" + classifier, nativeArtifact, minecraftRoot,
                                true, "linux", classifierArch(arch));
                    }
                }
            }
            return result;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Invalid Minecraft library metadata", e);
        }
    }

    private boolean allowed(JSONObject library) {
        JSONArray rules = library.optJSONArray("rules");
        if (rules == null || rules.length() == 0) return true;
        boolean allowed = false;
        for (int i = 0; i < rules.length(); i++) {
            JSONObject rule = rules.optJSONObject(i);
            if (rule == null) continue;
            String action = rule.optString("action", "").trim();
            JSONObject os = rule.optJSONObject("os");
            if (os == null) {
                allowed = "allow".equalsIgnoreCase(action);
                continue;
            }
            String name = os.optString("name", "").trim();
            if (name.isEmpty() || "linux".equalsIgnoreCase(name) || "android".equalsIgnoreCase(name)) {
                allowed = "allow".equalsIgnoreCase(action);
            }
        }
        return allowed;
    }

    private void add(List<LibraryDependency> result, String coordinate, JSONObject artifact,
                     File root, boolean nativeArtifact, String osName, String arch) throws Exception {
        String path = artifact.optString("path", "").trim();
        String urlValue = artifact.optString("url", "").trim();
        String sha1 = artifact.optString("sha1", "").trim();
        if (path.isEmpty() || urlValue.isEmpty() || sha1.isEmpty()) return;
        File file = safeRelative(new File(root, "libraries"), path);
        URL url = new URL(urlValue);
        result.add(new LibraryDependency(coordinate, url.toString(), sha1, osName, arch, file, nativeArtifact));
    }

    private String classifierArch(String path) {
        if (path.contains("arm64") || path.contains("aarch64")) return "arm64";
        if (path.contains("arm")) return "arm";
        if (path.contains("x86_64") || path.contains("amd64")) return "x86_64";
        if (path.contains("x86") || path.contains("i386") || path.contains("i686")) return "x86";
        return "";
    }

    private File safeRelative(File base, String path) throws IOException {
        if (path.startsWith("/") || path.startsWith("\\") || path.contains("..")) {
            throw new IOException("Unsafe library path: " + path);
        }
        File canonicalBase = base.getCanonicalFile();
        File result = new File(canonicalBase, path).getCanonicalFile();
        if (!result.toPath().startsWith(canonicalBase.toPath())) {
            throw new IOException("Unsafe library path: " + path);
        }
        return result;
    }
}
