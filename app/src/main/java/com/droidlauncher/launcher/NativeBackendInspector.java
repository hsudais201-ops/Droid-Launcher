package com.droidlauncher.launcher;

import android.content.Context;

import java.io.File;
import java.io.IOException;

/** Inspects native backend availability without downloading or modifying files. */
public final class NativeBackendInspector {
    private final NativeBackendManifestLoader manifestLoader;

    public NativeBackendInspector() {
        this(new NativeBackendManifestLoader());
    }

    public NativeBackendInspector(NativeBackendManifestLoader manifestLoader) {
        if (manifestLoader == null) throw new IllegalArgumentException("manifestLoader is required");
        this.manifestLoader = manifestLoader;
    }

    public NativeBackendStatus inspect(Context context, File backendRoot) {
        NativeAbi abi = NativeAbi.detect();
        String library = "libdroidglfw.so";
        File candidate = null;
        try {
            NativeBackendRegistry registry = manifestLoader.load(context);
            NativeBackendSpec spec = registry.forAbi(abi);
            library = spec.getLibraryFileName();
            candidate = new File(backendRoot, abi.getAndroidAbi() + File.separator + library).getCanonicalFile();
            boolean present = candidate.isFile() && candidate.length() > 0L;
            boolean verified = present && Sha1Verifier.verify(candidate, spec.getSha1());
            String message;
            if (abi == NativeAbi.UNKNOWN) {
                message = "Unsupported or unknown Android ABI";
            } else if (!present) {
                message = "Native backend is not installed";
            } else if (!verified) {
                message = "Native backend is present but checksum verification failed";
            } else {
                message = "Native backend is installed and verified";
            }
            return new NativeBackendStatus(abi, library, candidate, present, verified, message);
        } catch (Exception e) {
            return new NativeBackendStatus(abi, library, candidate, candidate != null && candidate.isFile(), false,
                    e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }
}
