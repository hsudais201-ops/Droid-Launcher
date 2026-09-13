package com.droidlauncher.launcher;

import java.io.File;
import java.net.URL;

/** One verified downloadable installation artifact. */
public final class DownloadTask {
    private final String name;
    private final URL url;
    private final File destination;
    private final String sha1;

    public DownloadTask(String name, URL url, File destination, String sha1) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("name is required");
        if (url == null) throw new IllegalArgumentException("url is required");
        if (destination == null) throw new IllegalArgumentException("destination is required");
        if (sha1 == null || sha1.trim().isEmpty()) throw new IllegalArgumentException("sha1 is required");
        this.name = name;
        this.url = url;
        this.destination = destination;
        this.sha1 = sha1.toLowerCase();
    }

    public String getName() { return name; }
    public URL getUrl() { return url; }
    public File getDestination() { return destination; }
    public String getSha1() { return sha1; }
}
