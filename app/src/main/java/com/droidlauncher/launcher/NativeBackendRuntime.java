package com.droidlauncher.launcher;

import android.content.Context;

import java.io.File;
import java.io.IOException;

/** Resolves and provisions the configured native backend for the current Android device. */
public final class NativeBackendRuntime {
    private final NativeBackendManifestLoader manifestLoader;

    public NativeBackendRuntime() {
        this(new NativeBackendManifestLoader());
    }

    public NativeBackendRuntime(NativeBackendManifestLoader manifestLoader) {
        if (manifestLoader == null) throw new IllegalArgumentException("manifestLoader is required");
        this.manifestLoader = manifestLoader;
    }

    public File prepare(Context context, File backendRoot) throws IOException {
        if (context == null) throw new IllegalArgumentException("context is required");
        if (backendRoot == null) throw new IllegalArgumentException("backendRoot is required");
        NativeAbi abi = NativeAbi.detect();
        NativeBackendRegistry registry = manifestLoader.load(context);
        NativeBackendSpec spec = registry.forAbi(abi);
        NativeBackendProvisioner provisioner = new NativeBackendProvisioner(new DownloadManager(), backendRoot);
        NativeBackendSelector selector = new NativeBackendSelector(backendRoot);
        return new NativeBackendLaunchGate(provisioner, selector).prepare(spec);
    }
}
