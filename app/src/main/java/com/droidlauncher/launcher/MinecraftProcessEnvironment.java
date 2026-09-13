package com.droidlauncher.launcher;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Builds the small, explicit environment needed by the Minecraft child process. */
public final class MinecraftProcessEnvironment {
    public Map<String, String> build(File nativesDirectory, String extraJavaLibraryPath) {
        if (nativesDirectory == null || !nativesDirectory.isDirectory()) {
            throw new IllegalArgumentException("Native directory is missing: " + nativesDirectory);
        }
        String nativePath = nativesDirectory.getAbsolutePath();
        if (extraJavaLibraryPath != null && !extraJavaLibraryPath.trim().isEmpty()) {
            nativePath = nativePath + File.pathSeparator + extraJavaLibraryPath.trim();
        }
        Map<String, String> values = new LinkedHashMap<>();
        values.put("DROID_LAUNCHER_NATIVE_DIR", nativesDirectory.getAbsolutePath());
        values.put("JAVA_LIBRARY_PATH", nativePath);
        return Collections.unmodifiableMap(values);
    }
}
