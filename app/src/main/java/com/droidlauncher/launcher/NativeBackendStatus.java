package com.droidlauncher.launcher;

import java.io.File;

/** Immutable status snapshot for the Android native rendering backend. */
public final class NativeBackendStatus {
    private final NativeAbi abi;
    private final String libraryFileName;
    private final File backendFile;
    private final boolean present;
    private final boolean verified;
    private final String message;

    public NativeBackendStatus(NativeAbi abi, String libraryFileName, File backendFile,
                               boolean present, boolean verified, String message) {
        this.abi = abi == null ? NativeAbi.UNKNOWN : abi;
        this.libraryFileName = libraryFileName == null ? "" : libraryFileName;
        this.backendFile = backendFile;
        this.present = present;
        this.verified = verified;
        this.message = message == null ? "" : message;
    }

    public NativeAbi getAbi() { return abi; }
    public String getLibraryFileName() { return libraryFileName; }
    public File getBackendFile() { return backendFile; }
    public boolean isPresent() { return present; }
    public boolean isVerified() { return verified; }
    public String getMessage() { return message; }
}
