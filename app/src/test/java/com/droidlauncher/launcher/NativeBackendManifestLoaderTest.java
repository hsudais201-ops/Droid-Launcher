package com.droidlauncher.launcher;

import org.junit.Test;

import java.util.EnumMap;

import static org.junit.Assert.*;

public class NativeBackendManifestLoaderTest {
    private static final String SHA1 = "0123456789012345678901234567890123456789";

    private static String manifest(String arm64Url, String arm64Sha1, String armeabiUrl,
                                   String armeabiSha1, String x64Url, String x64Sha1,
                                   String x86Url, String x86Sha1) {
        return "{\"schemaVersion\":1,\"library\":\"libdroidglfw.so\",\"backends\":{" +
                "\"arm64-v8a\":{\"url\":\"" + arm64Url + "\",\"sha1\":\"" + arm64Sha1 + "\"}," +
                "\"armeabi-v7a\":{\"url\":\"" + armeabiUrl + "\",\"sha1\":\"" + armeabiSha1 + "\"}," +
                "\"x86_64\":{\"url\":\"" + x64Url + "\",\"sha1\":\"" + x64Sha1 + "\"}," +
                "\"x86\":{\"url\":\"" + x86Url + "\",\"sha1\":\"" + x86Sha1 + "\"}}}";
    }

    @Test
    public void parseAcceptsCompleteManifest() throws Exception {
        NativeBackendRegistry registry = new NativeBackendManifestLoader().parse(manifest(
                "https://example.com/a", SHA1,
                "https://example.com/b", SHA1,
                "https://example.com/c", SHA1,
                "https://example.com/d", SHA1));

        assertEquals("libdroidglfw.so", registry.forAbi(NativeAbi.ARM64).getLibraryFileName());
        assertEquals(NativeAbi.X86, registry.forAbi(NativeAbi.X86).getAbi());
    }

    @Test
    public void parseRejectsMissingAbiEntry() throws Exception {
        String json = "{\"schemaVersion\":1,\"library\":\"libdroidglfw.so\",\"backends\":{" +
                "\"arm64-v8a\":{\"url\":\"https://example.com/a\",\"sha1\":\"" + SHA1 + "\"}" +
                "}}}";
        assertThrows(java.io.IOException.class, () -> new NativeBackendManifestLoader().parse(json));
    }

    @Test
    public void parseRejectsInvalidLibraryName() throws Exception {
        String json = manifest(
                "https://example.com/a", SHA1,
                "https://example.com/b", SHA1,
                "https://example.com/c", SHA1,
                "https://example.com/d", SHA1).replace("libdroidglfw.so", "../libevil.so");
        assertThrows(java.io.IOException.class, () -> new NativeBackendManifestLoader().parse(json));
    }

    @Test
    public void registryRejectsAbiMismatch() throws Exception {
        EnumMap<NativeAbi, NativeBackendSpec> specs = new EnumMap<>(NativeAbi.class);
        for (NativeAbi abi : NativeAbi.values()) {
            if (abi != NativeAbi.UNKNOWN) {
                specs.put(abi, new NativeBackendSpec(abi, "libdroidglfw.so",
                        new java.net.URL("https://example.com/" + abi.getAndroidAbi()), SHA1));
            }
        }
        specs.put(NativeAbi.X86, new NativeBackendSpec(NativeAbi.X86_64, "libdroidglfw.so",
                new java.net.URL("https://example.com/mismatch"), SHA1));
        assertThrows(IllegalArgumentException.class, () -> new NativeBackendRegistry(specs));
    }
}
