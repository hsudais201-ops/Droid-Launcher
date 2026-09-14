package com.droidlauncher.launcher;

import com.droidlauncher.runtime.JavaRuntime;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class LaunchPreflightTest {
    private final LaunchPreflight preflight = new LaunchPreflight();

    @Test
    public void rejectsMissingRuntime() {
        LaunchPreflight.Result result = preflight.validate(null,
                new File("game"), new File("natives"), "classpath", "Main");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Java runtime"));
    }

    @Test
    public void rejectsMissingGameDirectory() {
        File runtimeExecutable = new File(System.getProperty("java.home"), "bin/java");
        JavaRuntime runtime = new JavaRuntime(runtimeExecutable, "test", 17, true);
        LaunchPreflight.Result result = preflight.validate(runtime,
                new File("definitely-missing-game-directory"), new File("natives"), "classpath", "Main");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Game directory"));
    }

    @Test
    public void rejectsEmptyClasspath() {
        LaunchPreflight.Result result = preflight.validate(null, null, null, "", "Main");
        assertFalse(result.isValid());
    }

    @Test
    public void rejectsMissingMainClass() {
        LaunchPreflight.Result result = preflight.validate(null, null, null, "classpath", "");
        assertFalse(result.isValid());
    }
}
