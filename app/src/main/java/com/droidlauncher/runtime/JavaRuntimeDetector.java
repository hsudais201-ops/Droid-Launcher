package com.droidlauncher.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Discovers Java executables exposed by the Android environment and validates their versions. */
public final class JavaRuntimeDetector {
    private static final Pattern VERSION = Pattern.compile("version\\s+\\\"(\\d+)(?:\\.(\\d+))?");

    public List<JavaRuntime> detect() {
        Set<String> candidates = new LinkedHashSet<>();
        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(File.pathSeparator)) {
                if (!dir.isEmpty()) candidates.add(new File(dir, "java").getAbsolutePath());
            }
        }
        candidates.add("/data/data/com.droidlauncher/files/runtime/bin/java");
        candidates.add("/data/data/com.droidlauncher/files/java/bin/java");

        List<JavaRuntime> result = new ArrayList<>();
        for (String candidate : candidates) {
            File executable = new File(candidate);
            if (!executable.isFile() || !executable.canExecute()) continue;
            JavaRuntime runtime = inspect(executable);
            if (runtime != null) result.add(runtime);
        }
        return result;
    }

    private JavaRuntime inspect(File executable) {
        try {
            Process process = new ProcessBuilder(executable.getAbsolutePath(), "-version")
                    .redirectErrorStream(true)
                    .start();
            String firstLine;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                firstLine = reader.readLine();
            }
            int exitCode = process.waitFor();
            if (exitCode != 0 || firstLine == null) return null;

            Matcher matcher = VERSION.matcher(firstLine);
            if (!matcher.find()) return null;
            int major = Integer.parseInt(matcher.group(1));
            if (major == 1 && matcher.group(2) != null) major = Integer.parseInt(matcher.group(2));
            return new JavaRuntime(executable, major, firstLine);
        } catch (Exception ignored) {
            return null;
        }
    }
}
