package com.droidlauncher.runtime;

/** Minecraft profile Java compatibility requirement. */
public final class JavaRequirement {
    private final int minimumMajor;
    private final int maximumMajor;

    public JavaRequirement(int minimumMajor, int maximumMajor) {
        if (minimumMajor < 1 || maximumMajor < minimumMajor) {
            throw new IllegalArgumentException("Invalid Java version range");
        }
        this.minimumMajor = minimumMajor;
        this.maximumMajor = maximumMajor;
    }

    public boolean accepts(int major) {
        return major >= minimumMajor && major <= maximumMajor;
    }

    public int getMinimumMajor() { return minimumMajor; }
    public int getMaximumMajor() { return maximumMajor; }
}
