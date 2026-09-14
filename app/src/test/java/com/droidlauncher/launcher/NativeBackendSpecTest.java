package com.droidlauncher.launcher;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

public final class NativeBackendSpecTest {
    @Test
    public void acceptsValidSpec() throws Exception {
        NativeBackendSpec spec = new NativeBackendSpec(
                NativeAbi.ARM64,
                "libdroidglfw.so",
                new URL("https://example.com/libdroidglfw.so"),
                "0123456789012345678901234567890123456789");
        assertEquals(NativeAbi.ARM64, spec.getAbi());
        assertEquals("libdroidglfw.so", spec.getLibraryFileName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidSha1() throws Exception {
        new NativeBackendSpec(
                NativeAbi.X86,
                "libdroidglfw.so",
                new URL("https://example.com/libdroidglfw.so"),
                "not-a-sha1");
    }
}
