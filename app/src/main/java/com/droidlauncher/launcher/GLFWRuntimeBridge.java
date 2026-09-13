package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;

/** Entry point for future Android-compatible GLFW/LWJGL initialization. */
public final class GLFWRuntimeBridge {
    private final NativeLibraryLoader loader = new NativeLibraryLoader();

    public void initialize(NativeLaunchEnvironment environment, String glfwLibraryFileName)
            throws IOException {
        if (environment == null) throw new IllegalArgumentException("environment is required");
        if (glfwLibraryFileName == null || glfwLibraryFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("glfwLibraryFileName is required");
        }
        ensureAndroidCompatibleTarget(environment.getAbi());
        loader.load(environment.getNativeDirectory(), glfwLibraryFileName);
    }

    private void ensureAndroidCompatibleTarget(NativeAbi abi) throws IOException {
        if (abi == NativeAbi.UNKNOWN) {
            throw new IOException("Cannot initialize GLFW with an unknown Android ABI");
        }
        // CPU ABI validation is necessary but not sufficient. The native binary itself
        // must be built for Android; this bridge intentionally does not translate desktop binaries.
    }
}
