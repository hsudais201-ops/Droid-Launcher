package com.droidlauncher.launcher;

import org.junit.Test;

import java.io.File;
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
                "https://example.test/a", SHA1,
                "https://example.test/b", SHA1,
                "https://example.test/c", SHA1,
                "https://example.test/d", SHA1));

        assertEquals("libdroidglfw.so", registry.forAbi(NativeAbi.ARM64).getLibraryFileName());
        assertEquals(NativeAbi.X86, registry.forAbi(NativeAbi.X86).getAbi());
    }

    @Test
    public void parseRejectsMissingAbiEntry() {
        String json = "{\"schemaVersion\":1,\"library\":\"libdroidglfw.so\",\"backends\":{" +
                "\"arm64-v8a\":{\"url\":\"https://example.test/a\",\"sha1\":\"" + SHA1 + "\"}" +
                "}}";
        try {
            new NativeBackendManifestLoader().parse(json);
            fail("Expected missing ABI entry failure");
        } catch (Exception expected) {
            assertTrue(expected.getMessage().contains("Missing manifest entry"));
        }
    }

    @Test
    public void parseRejectsInvalidLibraryName() {
        String json = manifest(
                "https://example.test/a", SHA1,
                "https://example.test/b", SHA1,
                "https://example.test/c", SHA1,
                "https://example.test/d", SHA1).replace("libdroidglfw.so", "../libevil.so");
        try {
            new NativeBackendManifestLoader().parse(json);
            fail("Expected invalid library name failure");
        } catch (Exception expected) {
            assertTrue(expected.getMessage().contains("Invalid native backend library name"));
        }
    }

    @Test
    public void registryRejectsAbiMismatch() throws Exception {
        EnumMap<NativeAbi, NativeBackendSpec> specs = new EnumMap<>(NativeAbi.class);
        for (NativeAbi abi : NativeAbi.values()) {
            if (abi != NativeAbi.UNKNOWN) {
                specs.put(abi, new NativeBackendSpec(abi, "libdroidglfw.so",
                        new java.net.URL("https://example.test/" + abi.getAndroidAbi()), SHA1));
            }
        }
        specs.put(NativeAbi.X86, new NativeBackendSpec(NativeAbi.X86_64, "libdroidglfw.so",
                new java.net.URL("https://example.test/mismatch"), SHA1));
        try {
            new NativeBackendRegistry(specs);
            fail("Expected ABI mismatch failure");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("ABI mismatch"));
        }
    }
}
