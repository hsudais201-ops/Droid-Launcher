package com.droidlauncher.runtime;

import java.io.File;
import java.io.IOException;

/**
 * Resolves managed Java runtime locations and verifies that a runtime is usable.
 * Actual archive acquisition is deliberately delegated to the caller/download layer.
 */
public final class JavaRuntimeProvisioner {
    private final File runtimeRoot;
    private final JavaRuntimeDetector detector;

    public JavaRuntimeProvisioner(File runtimeRoot) {
        if (runtimeRoot == null) throw new IllegalArgumentException("runtimeRoot is required");
        this.runtimeRoot = runtimeRoot;
        this.detector = new JavaRuntimeDetector();
    }

    public File getRuntimeRoot() { return runtimeRoot; }

    public File executableFor(int majorVersion) {
        return new File(new File(runtimeRoot, "java-" + majorVersion), "bin/java");
    }

    public JavaRuntime require(int minimumMajor, int maximumMajor) throws IOException {
        JavaRequirement requirement = new JavaRequirement(minimumMajor, maximumMajor);
        File parent = runtimeRoot.getAbsoluteFile();
        if (!parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Cannot create Java runtime directory: " + parent);
        }

        for (int major = minimumMajor; major <= maximumMajor; major++) {
            File executable = executableFor(major);
            if (!executable.isFile() || !executable.canExecute()) continue;
            for (JavaRuntime runtime : detector.detect()) {
                if (runtime.getJavaExecutable().getAbsolutePath().equals(executable.getAbsolutePath())
                        && requirement.accepts(runtime.getMajorVersion())) {
                    return runtime;
                }
            }
        }
        throw new IOException("No managed Java runtime available for major versions "
                + minimumMajor + "-" + maximumMajor);
    }
}
