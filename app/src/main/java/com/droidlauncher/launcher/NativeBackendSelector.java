package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;

/** Selects the Android-native backend library for the current device ABI. */
public final class NativeBackendSelector {
    private final File jniLibsRoot;

    public NativeBackendSelector(File jniLibsRoot) {
        if (jniLibsRoot == null) throw new IllegalArgumentException("jniLibsRoot is required");
        this.jniLibsRoot = jniLibsRoot;
    }

    public File requireBackend(NativeAbi abi, String libraryFileName) throws IOException {
        if (abi == null || abi == NativeAbi.UNKNOWN) {
            throw new IOException("Unsupported Android ABI for native backend");
        }
        if (libraryFileName == null || libraryFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("libraryFileName is required");
        }
        String safeName = new File(libraryFileName).getName();
        if (!safeName.equals(libraryFileName) || !safeName.endsWith(".so")) {
            throw new IOException("Invalid native backend library name: " + libraryFileName);
        }
        File abiDirectory = new File(jniLibsRoot, abi.getAndroidAbi()).getCanonicalFile();
        File root = jniLibsRoot.getCanonicalFile();
        if (!abiDirectory.toPath().startsWith(root.toPath())) {
            throw new IOException("Unsafe native ABI directory");
        }
        File library = new File(abiDirectory, safeName).getCanonicalFile();
        if (!library.toPath().startsWith(abiDirectory.toPath())) {
            throw new IOException("Unsafe native backend path");
        }
        if (!library.isFile() || library.length() == 0L) {
            throw new IOException("Android-native backend is not packaged for " + abi.getAndroidAbi()
                    + ": " + library.getPath());
        }
        return library;
    }

    public File requireCurrentBackend(String libraryFileName) throws IOException {
        return requireBackend(NativeAbi.detect(), libraryFileName);
    }
}
