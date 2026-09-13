package com.droidlauncher.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Selects library artifacts that match the current Android runtime platform. */
public final class LibraryResolver {
    public List<LibraryDependency> resolve(List<LibraryDependency> candidates,
                                            String osName, String arch) {
        if (candidates == null) return Collections.emptyList();
        List<LibraryDependency> result = new ArrayList<>();
        for (LibraryDependency candidate : candidates) {
            if (candidate == null) continue;
            if (!matches(candidate, osName, arch)) continue;
            File file = candidate.getFile();
            if (file != null && file.isFile()) result.add(candidate);
        }
        return result;
    }

    private boolean matches(LibraryDependency dependency, String osName, String arch) {
        boolean osMatches = dependency.getOsName().isEmpty()
                || dependency.getOsName().equalsIgnoreCase(osName);
        boolean archMatches = dependency.getArch().isEmpty()
                || dependency.getArch().equalsIgnoreCase(arch);
        return osMatches && archMatches;
    }
}
