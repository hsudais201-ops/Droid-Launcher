package com.droidlauncher.launcher;

import android.view.Surface;

import java.io.File;
import java.io.IOException;

/**
 * Boundary for an Android-native GLFW/LWJGL runtime.
 *
 * The bridge validates the Android target and native library path before loading the library.
 * It deliberately does not convert a desktop GLFW binary into an Android renderer or fabricate
 * a GLFW window from an Android Surface; the loaded Android-native backend must provide that JNI
 * integration itself.
 */
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

    /**
     * Validates the Android Surface before handing a prepared native GLFW library to the future
     * JNI/EGL integration. Loading alone does not claim that a GLFW window/context was created.
     */
    public void initializeAndroidSurface(Surface surface,
                                         NativeLaunchEnvironment environment,
                                         String glfwLibraryFileName) throws IOException {
        if (surface == null || !surface.isValid()) {
            throw new IOException("Android rendering Surface is unavailable");
        }
        initialize(environment, glfwLibraryFileName);
    }

    public NativeAbi requireSupportedAbi() throws IOException {
        NativeAbi abi = NativeAbi.detect();
        ensureAndroidCompatibleTarget(abi);
        return abi;
    }

    private void ensureAndroidCompatibleTarget(NativeAbi abi) throws IOException {
        if (abi == null || abi == NativeAbi.UNKNOWN) {
            throw new IOException("Cannot initialize Android-native GLFW with an unknown ABI");
        }
        // ABI validation is necessary but not sufficient. The library itself must be compiled
        // for Android and must expose the JNI/EGL integration expected by the native backend.
    }
}
