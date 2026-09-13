package com.droidlauncher.launcher;

import android.os.Build;

/** Normalizes the device ABI used when selecting and extracting native Minecraft libraries. */
public enum NativeAbi {
    ARM64("arm64-v8a", "aarch64", "arm64"),
    ARM32("armeabi-v7a", "arm", "armv7"),
    X86_64("x86_64", "x86_64", "amd64"),
    X86("x86", "x86", "i386"),
    UNKNOWN("unknown", "unknown", "unknown");

    private final String androidAbi;
    private final String arch;
    private final String alternateArch;

    NativeAbi(String androidAbi, String arch, String alternateArch) {
        this.androidAbi = androidAbi;
        this.arch = arch;
        this.alternateArch = alternateArch;
    }

    public String getAndroidAbi() { return androidAbi; }
    public String getArch() { return arch; }
    public String getAlternateArch() { return alternateArch; }

    public static NativeAbi detect() {
        String[] abis = Build.SUPPORTED_ABIS;
        if (abis == null) return UNKNOWN;
        for (String abi : abis) {
            NativeAbi mapped = fromAndroidAbi(abi);
            if (mapped != UNKNOWN) return mapped;
        }
        return UNKNOWN;
    }

    public static NativeAbi fromAndroidAbi(String abi) {
        if (abi == null) return UNKNOWN;
        String value = abi.toLowerCase();
        if (value.contains("arm64")) return ARM64;
        if (value.contains("armeabi-v7a") || value.equals("armeabi")) return ARM32;
        if (value.contains("x86_64") || value.contains("amd64")) return X86_64;
        if (value.equals("x86") || value.contains("i686")) return X86;
        return UNKNOWN;
    }

    public boolean matchesArch(String value) {
        if (value == null || value.trim().isEmpty()) return true;
        String normalized = value.toLowerCase();
        return normalized.equals(arch) || normalized.equals(alternateArch)
                || normalized.equals(androidAbi);
    }
}
