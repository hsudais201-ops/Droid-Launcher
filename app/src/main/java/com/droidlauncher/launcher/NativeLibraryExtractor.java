package com.droidlauncher.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Extracts .so entries from a library JAR into a controlled native directory. */
public final class NativeLibraryExtractor {
    private static final int BUFFER_SIZE = 64 * 1024;

    public Set<File> extract(File jarFile, File nativeDirectory) throws IOException {
        if (jarFile == null || !jarFile.isFile()) {
            throw new IOException("Native library JAR is missing: " + jarFile);
        }
        if (nativeDirectory == null) throw new IllegalArgumentException("nativeDirectory is required");
        if (!nativeDirectory.exists() && !nativeDirectory.mkdirs()) {
            throw new IOException("Cannot create native directory: " + nativeDirectory);
        }

        File canonicalRoot = nativeDirectory.getCanonicalFile();
        String rootPrefix = canonicalRoot.getPath() + File.separator;
        Set<File> extracted = new HashSet<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (!name.endsWith(".so") && !name.contains(".so.")) continue;

                String fileName = new File(name).getName();
                if (fileName.isEmpty() || (!fileName.endsWith(".so") && !fileName.contains(".so."))) continue;
                File output = new File(canonicalRoot, fileName).getCanonicalFile();
                String outputPath = output.getPath();
                if (!outputPath.equals(canonicalRoot.getPath()) && !outputPath.startsWith(rootPrefix)) {
                    throw new IOException("Unsafe native entry: " + name);
                }

                try (BufferedInputStream in = new BufferedInputStream(jar.getInputStream(entry));
                     BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(output))) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int read;
                    while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
                }
                extracted.add(output);
            }
        }
        return extracted;
    }

    public void verifyReadable(Set<File> files) throws IOException {
        if (files == null) throw new IllegalArgumentException("files are required");
        for (File file : files) {
            if (file == null || !file.isFile() || !file.canRead() || file.length() == 0L) {
                throw new IOException("Extracted native library is unusable: " + file);
            }
            try (FileInputStream ignored = new FileInputStream(file)) {
                // Opening the file verifies it is readable before native loading.
            }
        }
    }
}
