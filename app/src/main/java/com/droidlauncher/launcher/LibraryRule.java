package com.droidlauncher.launcher;

/** Simple OS/architecture rule used when selecting a Minecraft library. */
public final class LibraryRule {
    private final String osName;
    private final String arch;
    private final boolean allow;

    public LibraryRule(String osName, String arch, boolean allow) {
        this.osName = osName == null ? "" : osName;
        this.arch = arch == null ? "" : arch;
        this.allow = allow;
    }

    public boolean matches(String currentOs, String currentArch) {
        boolean osMatches = osName.isEmpty() || osName.equalsIgnoreCase(currentOs);
        boolean archMatches = arch.isEmpty() || arch.equalsIgnoreCase(currentArch);
        return osMatches && archMatches;
    }

    public boolean isAllow() { return allow; }
    public String getOsName() { return osName; }
    public String getArch() { return arch; }
}
