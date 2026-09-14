package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;

/**
 * Launch-time gate that ensures the ABI-specific Android native backend is present and verified
 * before GLFW/LWJGL initialization is attempted.
 */
public final class NativeBackendLaunchGate {
    private final NativeBackendProvisioner provisioner;
    private final NativeBackendSelector selector;

    public NativeBackendLaunchGate(NativeBackendProvisioner provisioner, NativeBackendSelector selector) {
        if (provisioner == null || selector == null) {
            throw new IllegalArgumentException("Native backend launch dependencies are required");
        }
        this.provisioner = provisioner;
        this.selector = selector;
    }

    public File prepare(NativeBackendSpec spec) throws IOException {
        if (spec == null) throw new IllegalArgumentException("spec is required");
        NativeAbi current = NativeAbi.detect();
        if (current == NativeAbi.UNKNOWN) {
            throw new IOException("Cannot prepare native backend: Android ABI is unknown");
        }
        if (current != spec.getAbi()) {
            throw new IOException("Native backend ABI mismatch: device=" + current.getAndroidAbi()
                    + ", backend=" + spec.getAbi().getAndroidAbi());
        }
        File installed = provisioner.provision(spec);
        File selected = selector.requireCurrentBackend(spec.getLibraryFileName());
        if (!installed.getCanonicalFile().equals(selected.getCanonicalFile())) {
            throw new IOException("Native backend selection mismatch after verification");
        }
        return selected;
    }
}
