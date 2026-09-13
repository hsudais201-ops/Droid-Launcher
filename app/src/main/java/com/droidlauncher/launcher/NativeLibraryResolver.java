package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Resolves native JARs for the detected Android ABI without silently accepting incompatible binaries. */
public final class NativeLibraryResolver {
    public List<LibraryDependency> resolve(List<LibraryDependency> candidates, NativeAbi abi) {
        if (candidates == null || abi == null || abi == NativeAbi.UNKNOWN) {
            return Collections.emptyList();
        }
        List<LibraryDependency> result = new ArrayList<>();
        for (LibraryDependency dependency : candidates) {
            if (dependency == null || !dependency.isNativeArtifact()) continue;
            if (!dependency.getArch().isEmpty() && !abi.matchesArch(dependency.getArch())) continue;
            if (!dependency.getOsName().isEmpty()
                    && !"linux".equalsIgnoreCase(dependency.getOsName())
                    && !"android".equalsIgnoreCase(dependency.getOsName())) continue;
            if (dependency.getFile() == null || !dependency.getFile().isFile()) {
                throw new IllegalStateException("Missing native artifact: " + dependency.getCoordinate());
            }
            result.add(dependency);
        }
        return result;
    }

    public void requireAbi(NativeAbi abi) throws IOException {
        if (abi == null || abi == NativeAbi.UNKNOWN) {
            throw new IOException("Unsupported Android CPU ABI; cannot load Minecraft natives safely");
        }
    }
}
