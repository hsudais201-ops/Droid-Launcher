package com.droidlauncher.launcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

/** Immutable registry for ABI-specific Android native backend download metadata. */
public final class NativeBackendRegistry {
    private final Map<NativeAbi, NativeBackendSpec> specs;

    public NativeBackendRegistry(Map<NativeAbi, NativeBackendSpec> specs) {
        if (specs == null) throw new IllegalArgumentException("specs are required");
        EnumMap<NativeAbi, NativeBackendSpec> copy = new EnumMap<>(NativeAbi.class);
        for (NativeAbi abi : NativeAbi.values()) {
            if (abi == NativeAbi.UNKNOWN) continue;
            NativeBackendSpec spec = specs.get(abi);
            if (spec == null) throw new IllegalArgumentException("Missing native backend spec for " + abi.getAndroidAbi());
            if (spec.getAbi() != abi) throw new IllegalArgumentException("Native backend spec ABI mismatch for " + abi.getAndroidAbi());
            copy.put(abi, spec);
        }
        this.specs = copy;
    }

    public NativeBackendSpec forAbi(NativeAbi abi) throws IOException {
        if (abi == null || abi == NativeAbi.UNKNOWN) throw new IOException("Unsupported Android ABI");
        NativeBackendSpec spec = specs.get(abi);
        if (spec == null) throw new IOException("No native backend configuration for " + abi.getAndroidAbi());
        return spec;
    }

    public static NativeBackendRegistry emptyConfiguration() {
        EnumMap<NativeAbi, NativeBackendSpec> specs = new EnumMap<>(NativeAbi.class);
        for (NativeAbi abi : NativeAbi.values()) {
            if (abi != NativeAbi.UNKNOWN) specs.put(abi, unavailable(abi));
        }
        return new NativeBackendRegistry(specs);
    }

    private static NativeBackendSpec unavailable(NativeAbi abi) {
        try {
            return new NativeBackendSpec(
                    abi,
                    "libdroidglfw.so",
                    new URL("https://invalid.droidlauncher.invalid/native/" + abi.getAndroidAbi() + "/libdroidglfw.so"),
                    "0000000000000000000000000000000000000000");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}
