package com.droidlauncher.launcher;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public final class NativeBackendSelectorTest {
    @Test(expected = java.io.IOException.class)
    public void rejectsMissingBackend() throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"), "droid-launcher-native-test");
        new NativeBackendSelector(root).requireBackend(NativeAbi.ARM64, "libdroidglfw.so");
    }

    @Test(expected = java.io.IOException.class)
    public void rejectsPathTraversalLibraryName() throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"), "droid-launcher-native-test");
        new NativeBackendSelector(root).requireBackend(NativeAbi.ARM64, "../libdroidglfw.so");
    }
}
