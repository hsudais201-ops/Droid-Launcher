package com.droidlauncher.launcher;

import java.io.File;

/** One downloadable Minecraft library artifact and its optional platform rules. */
public final class LibraryDependency {
    private final String coordinate;
    private final String url;
    private final String sha1;
    private final String osName;
    private final String arch;
    private final File file;
    private final boolean nativeArtifact;

    public LibraryDependency(String coordinate, String url, String sha1,
                             String osName, String arch, File file, boolean nativeArtifact) {
        if (coordinate == null || coordinate.trim().isEmpty()) {
            throw new IllegalArgumentException("coordinate is required");
        }
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("url is required");
        }
        this.coordinate = coordinate;
        this.url = url;
        this.sha1 = sha1 == null ? "" : sha1;
        this.osName = osName == null ? "" : osName;
        this.arch = arch == null ? "" : arch;
        this.file = file;
        this.nativeArtifact = nativeArtifact;
    }

    public String getCoordinate() { return coordinate; }
    public String getUrl() { return url; }
    public String getSha1() { return sha1; }
    public String getOsName() { return osName; }
    public String getArch() { return arch; }
    public File getFile() { return file; }
    public boolean isNativeArtifact() { return nativeArtifact; }
}
