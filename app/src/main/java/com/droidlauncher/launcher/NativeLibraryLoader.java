package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;

/** Loads a prepared native library from a controlled directory. */
public final class NativeLibraryLoader {
    public void load(File nativeDirectory, String libraryFileName) throws IOException {
        if (nativeDirectory == null || libraryFileName == null || libraryFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("nativeDirectory and libraryFileName are required");
        }
        File root = nativeDirectory.getCanonicalFile();
        File file = new File(root, libraryFileName).getCanonicalFile();
        if (!file.toPath().startsWith(root.toPath())) {
            throw new IOException("Unsafe native library path: " + libraryFileName);
        }
        if (!file.isFile() || file.length() == 0L) {
            throw new IOException("Native library is missing or empty: " + file);
        }
        try {
            System.load(file.getAbsolutePath());
        } catch (UnsatisfiedLinkError error) {
            throw new IOException("Native library could not be loaded: " + file, error);
        }
    }
}
