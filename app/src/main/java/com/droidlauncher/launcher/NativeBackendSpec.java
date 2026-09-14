package com.droidlauncher.launcher;

import java.net.URL;

/** Immutable description of one downloadable Android-native backend binary. */
public final class NativeBackendSpec {
    private final NativeAbi abi;
    private final String libraryFileName;
    private final URL downloadUrl;
    private final String sha1;

    public NativeBackendSpec(NativeAbi abi, String libraryFileName, URL downloadUrl, String sha1) {
        if (abi == null || abi == NativeAbi.UNKNOWN) throw new IllegalArgumentException("Supported ABI is required");
        if (libraryFileName == null || libraryFileName.trim().isEmpty()) throw new IllegalArgumentException("libraryFileName is required");
        if (downloadUrl == null) throw new IllegalArgumentException("downloadUrl is required");
        if (sha1 == null || !sha1.matches("(?i)[0-9a-f]{40}")) throw new IllegalArgumentException("A 40-character SHA-1 is required");
        this.abi = abi;
        this.libraryFileName = libraryFileName;
        this.downloadUrl = downloadUrl;
        this.sha1 = sha1;
    }

    public NativeAbi getAbi() { return abi; }
    public String getLibraryFileName() { return libraryFileName; }
    public URL getDownloadUrl() { return downloadUrl; }
    public String getSha1() { return sha1; }
}
