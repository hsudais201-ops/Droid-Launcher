package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Resolves the installed Minecraft client and library JARs into a launch-ready classpath. */
public final class MinecraftLaunchClasspath {
    private final MinecraftLibraryMetadataParser parser;
    private final ClasspathBuilder classpathBuilder;

    public MinecraftLaunchClasspath() {
        this(new MinecraftLibraryMetadataParser(), new ClasspathBuilder());
    }

    public MinecraftLaunchClasspath(MinecraftLibraryMetadataParser parser,
                                    ClasspathBuilder classpathBuilder) {
        if (parser == null || classpathBuilder == null) throw new IllegalArgumentException("dependencies are required");
        this.parser = parser;
        this.classpathBuilder = classpathBuilder;
    }

    public Result resolve(String versionJson, File minecraftRoot) throws IOException {
        if (versionJson == null || versionJson.trim().isEmpty()) throw new IOException("version JSON is empty");
        if (minecraftRoot == null) throw new IOException("minecraft root is required");
        List<LibraryDependency> all = parser.parse(versionJson, minecraftRoot);
        List<LibraryDependency> jvm = new ArrayList<>();
        List<LibraryDependency> natives = new ArrayList<>();
        for (LibraryDependency dependency : all) {
            if (dependency.isNativeArtifact()) natives.add(dependency);
            else jvm.add(dependency);
        }
        String librariesClasspath = classpathBuilder.build(jvm);
        String clientJar = extractClientJarPath(versionJson, minecraftRoot);
        String classpath = clientJar + (librariesClasspath.isEmpty() ? "" : File.pathSeparator + librariesClasspath);
        return new Result(classpath, jvm, natives);
    }

    private String extractClientJarPath(String versionJson, File root) throws IOException {
        try {
            org.json.JSONObject json = new org.json.JSONObject(versionJson);
            String id = json.optString("id", "").trim();
            if (id.isEmpty()) throw new IOException("Minecraft version id is missing");
            File client = VersionInstallation.clientJar(root, id).getCanonicalFile();
            if (!client.isFile()) throw new IOException("Minecraft client JAR is missing: " + client);
            return client.getAbsolutePath();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Invalid Minecraft version metadata", e);
        }
    }

    public static final class Result {
        private final String classpath;
        private final List<LibraryDependency> libraries;
        private final List<LibraryDependency> nativeLibraries;

        Result(String classpath, List<LibraryDependency> libraries, List<LibraryDependency> nativeLibraries) {
            this.classpath = classpath;
            this.libraries = libraries;
            this.nativeLibraries = nativeLibraries;
        }

        public String getClasspath() { return classpath; }
        public List<LibraryDependency> getLibraries() { return libraries; }
        public List<LibraryDependency> getNativeLibraries() { return nativeLibraries; }
    }
}
