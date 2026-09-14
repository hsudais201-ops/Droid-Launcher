package com.droidlauncher.launcher;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class NativeAbiTest {
    @Test
    public void mapsAndroidAbis() {
        assertEquals(NativeAbi.ARM64, NativeAbi.fromAndroidAbi("arm64-v8a"));
        assertEquals(NativeAbi.ARM32, NativeAbi.fromAndroidAbi("armeabi-v7a"));
        assertEquals(NativeAbi.X86_64, NativeAbi.fromAndroidAbi("x86_64"));
        assertEquals(NativeAbi.X86, NativeAbi.fromAndroidAbi("x86"));
        assertEquals(NativeAbi.UNKNOWN, NativeAbi.fromAndroidAbi("mips"));
    }
}
