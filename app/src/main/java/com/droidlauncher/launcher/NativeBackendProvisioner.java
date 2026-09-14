package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/** Downloads, verifies, and atomically installs an Android-native backend for one ABI. */
public final class NativeBackendProvisioner {
    private final DownloadManager downloadManager;
    private final File backendRoot;

    public NativeBackendProvisioner(DownloadManager downloadManager, File backendRoot) {
        if (downloadManager == null) throw new IllegalArgumentException("downloadManager is required");
        if (backendRoot == null) throw new IllegalArgumentException("backendRoot is required");
        this.downloadManager = downloadManager;
        this.backendRoot = backendRoot;
    }

    public File provision(NativeBackendSpec spec) throws IOException {
        if (spec == null) throw new IllegalArgumentException("spec is required");
        URL url = spec.getDownloadUrl();
        if (!"https".equalsIgnoreCase(url.getProtocol())) {
            throw new IOException("Native backend downloads must use HTTPS");
        }

        File abiDirectory = new File(backendRoot, spec.getAbi().getAndroidAbi()).getCanonicalFile();
        File root = backendRoot.getCanonicalFile();
        if (!abiDirectory.toPath().startsWith(root.toPath())) {
            throw new IOException("Unsafe native backend directory");
        }
        if (!abiDirectory.exists() && !abiDirectory.mkdirs() && !abiDirectory.isDirectory()) {
            throw new IOException("Cannot create native backend directory");
        }

        File destination = new File(abiDirectory, spec.getLibraryFileName()).getCanonicalFile();
        if (!destination.toPath().startsWith(abiDirectory.toPath())) {
            throw new IOException("Unsafe native backend path");
        }

        if (Sha1Verifier.verify(destination, spec.getSha1())) return destination;

        File partial = new File(destination.getPath() + ".part").getCanonicalFile();
        if (!partial.toPath().startsWith(abiDirectory.toPath())) {
            throw new IOException("Unsafe partial backend path");
        }
        downloadManager.download(url, partial);
        if (!Sha1Verifier.verify(partial, spec.getSha1())) {
            if (!partial.delete() && partial.exists()) {
                throw new IOException("Native backend checksum failed and partial file could not be removed");
            }
            throw new IOException("Native backend checksum verification failed for " + spec.getAbi().getAndroidAbi());
        }

        if (destination.exists() && !destination.delete()) {
            throw new IOException("Cannot replace existing native backend");
        }
        if (!partial.renameTo(destination)) {
            throw new IOException("Cannot install verified native backend");
        }
        if (!Sha1Verifier.verify(destination, spec.getSha1())) {
            if (!destination.delete() && destination.exists()) {
                throw new IOException("Installed native backend failed verification and could not be removed");
            }
            throw new IOException("Installed native backend failed final checksum verification");
        }
        return destination;
    }
}
