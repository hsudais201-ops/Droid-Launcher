package com.droidlauncher.launcher;

import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class NativeBackendProvisionerTest {
    @Test(expected = java.io.IOException.class)
    public void rejectsNonHttpsBackendUrl() throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"), "droid-launcher-provisioner-http-test");
        NativeBackendSpec spec = new NativeBackendSpec(
                NativeAbi.ARM64,
                "libdroidglfw.so",
                new URL("http://example.invalid/libdroidglfw.so"),
                "0123456789012345678901234567890123456789");
        new NativeBackendProvisioner(new DownloadManager(), root).provision(spec);
    }

    @Test
    public void checksumVerifierRejectsWrongHash() throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"), "droid-launcher-sha-test");
        if (!root.exists()) assertTrue(root.mkdirs());
        File file = new File(root, "payload.bin");
        java.nio.file.Files.write(file.toPath(), "known-good-payload".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertFalse(Sha1Verifier.verify(file, "0000000000000000000000000000000000000000"));
        assertTrue(file.delete() || !file.exists());
        assertTrue(root.delete() || !root.exists());
    }

    @Test
    public void validChecksumIsAccepted() throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"), "droid-launcher-sha-valid-test");
        if (!root.exists()) assertTrue(root.mkdirs());
        File file = new File(root, "payload.bin");
        byte[] payload = "known-good-payload".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        java.nio.file.Files.write(file.toPath(), payload);
        String expected = "b1d4c2f29ad7617d1bfe8ce75e1f12fd70b80f1b";
        assertTrue(Sha1Verifier.verify(file, expected));
        assertTrue(file.delete() || !file.exists());
        assertTrue(root.delete() || !root.exists());
    }
}
