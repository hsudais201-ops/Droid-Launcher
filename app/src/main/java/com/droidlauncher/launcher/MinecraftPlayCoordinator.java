package com.droidlauncher.launcher;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/** Prepares Minecraft installation, classpath, and native directory before the real process launch. */
public final class MinecraftPlayCoordinator {
    private final MinecraftInstallationService installationService;
    private final MinecraftDownloadOrchestrator downloadOrchestrator;
    private final MinecraftLaunchClasspath classpathResolver;
    private final MinecraftNativePreparer nativePreparer;

    public MinecraftPlayCoordinator() {
        this(new MinecraftInstallationService(), new MinecraftDownloadOrchestrator(),
                new MinecraftLaunchClasspath(), new MinecraftNativePreparer());
    }

    public MinecraftPlayCoordinator(MinecraftInstallationService installationService,
                                    MinecraftDownloadOrchestrator downloadOrchestrator,
                                    MinecraftLaunchClasspath classpathResolver,
                                    MinecraftNativePreparer nativePreparer) {
        if (installationService == null || downloadOrchestrator == null
                || classpathResolver == null || nativePreparer == null) {
            throw new IllegalArgumentException("dependencies are required");
        }
        this.installationService = installationService;
        this.downloadOrchestrator = downloadOrchestrator;
        this.classpathResolver = classpathResolver;
        this.nativePreparer = nativePreparer;
    }

    public PreparedLaunch prepare(String versionId, File minecraftRoot, File nativeDirectory,
                                  DownloadProgressListener listener) throws IOException {
        MinecraftInstallationService.InstallationPlan plan =
                installationService.prepare(versionId, minecraftRoot);
        downloadOrchestrator.install(plan.getTasks(), listener);

        MinecraftLaunchClasspath.Result classpath =
                classpathResolver.resolve(plan.getVersionJson(), minecraftRoot);
        File natives = nativePreparer.prepare(classpath.getNativeLibraries(), NativeAbi.detect(), nativeDirectory);
        return new PreparedLaunch(plan.getMetadata().getId(), plan.getMetadata().getMainClass(),
                classpath.getClasspath(), natives);
    }

    public PreparedLaunch prepareInstalled(String versionJson, File minecraftRoot, File nativeDirectory)
            throws IOException {
        MinecraftLaunchClasspath.Result classpath = classpathResolver.resolve(versionJson, minecraftRoot);
        File natives = nativePreparer.prepare(classpath.getNativeLibraries(), NativeAbi.detect(), nativeDirectory);
        try {
            org.json.JSONObject json = new org.json.JSONObject(versionJson);
            String id = json.optString("id", "");
            String mainClass = json.optString("mainClass", "");
            if (id.isEmpty() || mainClass.isEmpty()) throw new IOException("Incomplete Minecraft version metadata");
            return new PreparedLaunch(id, mainClass, classpath.getClasspath(), natives);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Invalid Minecraft version metadata", e);
        }
    }

    public static final class PreparedLaunch {
        private final String versionId;
        private final String mainClass;
        private final String classpath;
        private final File nativeDirectory;

        PreparedLaunch(String versionId, String mainClass, String classpath, File nativeDirectory) {
            this.versionId = versionId;
            this.mainClass = mainClass;
            this.classpath = classpath;
            this.nativeDirectory = nativeDirectory;
        }

        public String getVersionId() { return versionId; }
        public String getMainClass() { return mainClass; }
        public String getClasspath() { return classpath; }
        public File getNativeDirectory() { return nativeDirectory; }
    }
}
