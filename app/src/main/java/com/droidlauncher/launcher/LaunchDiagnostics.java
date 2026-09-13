package com.droidlauncher.launcher;

import java.util.Locale;

/** Classifies launch/process failures into actionable diagnostics without hiding the raw error. */
public final class LaunchDiagnostics {
    public enum Category {
        NONE, JAVA_RUNTIME, CLASSPATH, NATIVE_LIBRARY, GLFW, MEMORY, MOD_LOADER, PROCESS, UNKNOWN
    }

    private LaunchDiagnostics() { }

    public static Category classify(String message) {
        if (message == null || message.trim().isEmpty()) return Category.NONE;
        String value = message.toLowerCase(Locale.ROOT);
        if (value.contains("java" ) && (value.contains("version") || value.contains("executable")
                || value.contains("runtime"))) return Category.JAVA_RUNTIME;
        if (value.contains("classnotfound") || value.contains("noclassdeffound")
                || value.contains("classpath") || value.contains("could not find or load main class")) {
            return Category.CLASSPATH;
        }
        if (value.contains("unsatisfiedlinkerror") || value.contains("jni")
                || value.contains(".so") || value.contains("native library")) return Category.NATIVE_LIBRARY;
        if (value.contains("glfw")) return Category.GLFW;
        if (value.contains("outofmemory") || value.contains("java heap space")
                || value.contains("memory")) return Category.MEMORY;
        if (value.contains("fabric") || value.contains("forge") || value.contains("neoforge")
                || value.contains("mod loader")) return Category.MOD_LOADER;
        if (value.contains("process") || value.contains("permission denied")
                || value.contains("failed to start")) return Category.PROCESS;
        return Category.UNKNOWN;
    }

    public static String hint(Category category) {
        switch (category) {
            case JAVA_RUNTIME: return "Check that the selected Java runtime exists and satisfies the Minecraft version.";
            case CLASSPATH: return "Rebuild or redownload required libraries and verify their checksums.";
            case NATIVE_LIBRARY: return "Check ABI compatibility and that Android-compatible native libraries were extracted.";
            case GLFW: return "Verify the Android GLFW integration and graphics surface are initialized before Minecraft.";
            case MEMORY: return "Lower the configured heap size or close other apps before launching.";
            case MOD_LOADER: return "Verify the selected loader and its libraries match the Minecraft version.";
            case PROCESS: return "Check executable permissions, working-directory access, and launch arguments.";
            case UNKNOWN: return "Inspect the full Minecraft log and process stderr for the original failure.";
            default: return "";
        }
    }
}
