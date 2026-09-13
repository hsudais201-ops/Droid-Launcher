package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Resumable HTTP downloader. Network work is intentionally performed off the Android UI thread.
 */
public final class DownloadManager {
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 30000;
    private static final int BUFFER_SIZE = 64 * 1024;

    public void download(URL source, File destination) throws IOException {
        if (source == null || destination == null) throw new IllegalArgumentException("source and destination are required");
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Cannot create download directory");
        }

        long existing = destination.isFile() ? destination.length() : 0L;
        HttpURLConnection connection = (HttpURLConnection) source.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("Accept-Encoding", "identity");
        if (existing > 0) connection.setRequestProperty("Range", "bytes=" + existing + "-");

        int code = connection.getResponseCode();
        boolean append = existing > 0 && code == HttpURLConnection.HTTP_PARTIAL;
        if (code != HttpURLConnection.HTTP_OK && !append) {
            throw new IOException("Download failed with HTTP " + code);
        }

        long offset = append ? existing : 0L;
        try (RandomAccessFile out = new RandomAccessFile(destination, "rw")) {
            out.seek(offset);
            byte[] buffer = new byte[BUFFER_SIZE];
            try (java.io.InputStream in = new java.io.BufferedInputStream(connection.getInputStream(), BUFFER_SIZE)) {
                int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            }
        } finally {
            connection.disconnect();
        }
    }
}
