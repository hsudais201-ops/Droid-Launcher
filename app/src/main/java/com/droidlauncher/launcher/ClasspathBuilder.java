package com.droidlauncher.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Builds a deterministic, duplicate-free Java classpath from resolved library JARs. */
public final class ClasspathBuilder {
    public String build(List<LibraryDependency> dependencies) {
        if (dependencies == null) throw new IllegalArgumentException("dependencies are required");

        List<File> files = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (LibraryDependency dependency : dependencies) {
            if (dependency == null || dependency.getFile() == null) continue;
            File file = dependency.getFile();
            if (!file.isFile()) {
                throw new IllegalStateException("Missing library artifact: " + file);
            }
            String key = file.getAbsolutePath();
            if (seen.add(key)) files.add(file);
        }
        files.sort(Comparator.comparing(File::getAbsolutePath));

        String separator = File.pathSeparator;
        StringBuilder classpath = new StringBuilder();
        for (File file : files) {
            if (classpath.length() > 0) classpath.append(separator);
            classpath.append(file.getAbsolutePath());
        }
        return classpath.toString();
    }
}
