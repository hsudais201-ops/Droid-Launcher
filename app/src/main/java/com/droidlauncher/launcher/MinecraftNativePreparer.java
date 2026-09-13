package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/** Selects Android-ABI natives, extracts them, and validates the output before launch. */
public final class MinecraftNativePreparer {
    private final NativeLibraryResolver resolver;
    private final NativeLibraryExtractor extractor;

    public MinecraftNativePreparer() {
        this(new NativeLibraryResolver(), new NativeLibraryExtractor());
    }

    public MinecraftNativePreparer(NativeLibraryResolver resolver, NativeLibraryExtractor extractor) {
        if (resolver == null || extractor == null) throw new IllegalArgumentException("dependencies are required");
        this.resolver = resolver;
        this.extractor = extractor;
    }

    public File prepare(List<LibraryDependency> candidates, NativeAbi abi, File nativeDirectory) throws IOException {
        resolver.requireAbi(abi);
        if (nativeDirectory == null) throw new IOException("native directory is required");
        List<LibraryDependency> selected = resolver.resolve(candidates, abi);
        if (selected.isEmpty()) {
            throw new IOException("No compatible Minecraft native library was found for ABI " + abi.getAndroidAbi());
        }
        for (LibraryDependency dependency : selected) {
            Set<File> extracted = extractor.extract(dependency.getFile(), nativeDirectory);
            extractor.verifyReadable(extracted);
        }
        return nativeDirectory.getCanonicalFile();
    }
}
