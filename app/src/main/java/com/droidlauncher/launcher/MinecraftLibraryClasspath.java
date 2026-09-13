package com.droidlauncher.launcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Resolves installed Minecraft library artifacts into a launch classpath. */
public final class MinecraftLibraryClasspath {
    public List<LibraryDependency> resolve(String versionJson, File minecraftRoot) throws IOException {
        if (versionJson == null || versionJson.trim().isEmpty()) throw new IOException("version JSON is empty");
        if (minecraftRoot == null) throw new IOException("minecraft root is required");
        try {
            JSONObject root = new JSONObject(versionJson);
            JSONArray libraries = root.optJSONArray("libraries");
            List<LibraryDependency> result = new ArrayList<>();
            if (libraries == null) return result;
            for (int i = 0; i < libraries.length(); i++) {
                JSONObject lib = libraries.optJSONObject(i);
                if (lib == null) continue;
                JSONObject downloads = lib.optJSONObject("downloads");
                JSONObject artifact = downloads == null ? null : downloads.optJSONObject("artifact");
                if (artifact == null) continue;
                String path = artifact.optString("path", "").trim();
                if (path.isEmpty()) continue;
                File file = safeLibraryFile(minecraftRoot, path);
                result.add(new LibraryDependency(
                        lib.optString("name", "library-" + i),
                        artifact.optString("url", ""),
                        artifact.optString("sha1", ""),
                        "", "", file, false));
            }
            return result;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Invalid library metadata", e);
        }
    }

    private File safeLibraryFile(File root, String path) throws IOException {
        if (path.startsWith("/") || path.startsWith("\\") || path.contains("..")) {
            throw new IOException("Unsafe library path: " + path);
        }
        File base = new File(root, "libraries").getCanonicalFile();
        File file = new File(base, path).getCanonicalFile();
        if (!file.toPath().startsWith(base.toPath())) throw new IOException("Unsafe library path: " + path);
        return file;
    }
}
