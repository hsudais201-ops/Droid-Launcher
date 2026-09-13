package com.droidlauncher.runtime;

import java.io.File;

/** Immutable description of a Java runtime available to the launcher. */
public final class JavaRuntime {
    private final File javaExecutable;
    private final int majorVersion;
    private final String rawVersion;

    public JavaRuntime(File javaExecutable, int majorVersion, String rawVersion) {
        this.javaExecutable = javaExecutable;
        this.majorVersion = majorVersion;
        this.rawVersion = rawVersion;
    }

    public File getJavaExecutable() { return javaExecutable; }
    public int getMajorVersion() { return majorVersion; }
    public String getRawVersion() { return rawVersion; }
}
