package com.droidlauncher.launcher;

import com.droidlauncher.runtime.JavaRuntime;

import java.io.File;

/** Validates the minimum filesystem/runtime requirements before starting Minecraft. */
public final class LaunchPreflight {
    public Result validate(JavaRuntime runtime,
                           File gameDirectory,
                           File nativesDirectory,
                           String classpath,
                           String mainClass) {
        if (runtime == null || runtime.getJavaExecutable() == null
                || !runtime.getJavaExecutable().isFile()) {
            return Result.fail("Java runtime is missing or invalid");
        }
        if (gameDirectory == null || !gameDirectory.isDirectory()) {
            return Result.fail("Game directory is missing: " + gameDirectory);
        }
        if (nativesDirectory == null || !nativesDirectory.isDirectory()) {
            return Result.fail("Native directory is missing: " + nativesDirectory);
        }
        if (classpath == null || classpath.trim().isEmpty()) {
            return Result.fail("Classpath is empty");
        }
        if (mainClass == null || mainClass.trim().isEmpty()) {
            return Result.fail("Minecraft main class is missing");
        }
        return Result.ok();
    }

    public static final class Result {
        private final boolean valid;
        private final String message;

        private Result(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static Result ok() { return new Result(true, "OK"); }
        public static Result fail(String message) { return new Result(false, message); }
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}
