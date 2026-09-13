package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;

/** Describes the native search path prepared for the Minecraft Java process. */
public final class NativeLaunchEnvironment {
    private final NativeAbi abi;
    private final File nativeDirectory;

    public NativeLaunchEnvironment(NativeAbi abi, File nativeDirectory) throws IOException {
        if (abi == null || abi == NativeAbi.UNKNOWN) {
            throw new IOException("Unsupported Android ABI");
        }
        if (nativeDirectory == null) throw new IllegalArgumentException("nativeDirectory is required");
        this.abi = abi;
        this.nativeDirectory = nativeDirectory.getCanonicalFile();
        if (!this.nativeDirectory.isDirectory()) {
            throw new IOException("Native directory does not exist: " + this.nativeDirectory);
        }
    }

    public NativeAbi getAbi() { return abi; }
    public File getNativeDirectory() { return nativeDirectory; }
    public String getJavaLibraryPath() { return nativeDirectory.getAbsolutePath(); }
}
