package com.droidlauncher.launcher;

import com.droidlauncher.runtime.JavaRuntime;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        JavaRuntime runtime = new JavaRuntime(runtimeExecutable, 17, "test");
        LaunchPreflight.Result result = preflight.validate(runtime,
                new File("definitely-missing-game-directory"), new File("natives"), "classpath", "Main");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Game directory"));
    }

    @Test
    public void rejectsMissingNativesDirectory() {
        File runtimeExecutable = new File(System.getProperty("java.home"), "bin/java");
        JavaRuntime runtime = new JavaRuntime(runtimeExecutable, 17, "test");
        File game = new File(System.getProperty("java.io.tmpdir"), "droid-preflight-game");
        assertTrue(game.mkdirs() || game.isDirectory());
        LaunchPreflight.Result result = preflight.validate(runtime,
                game, new File("definitely-missing-natives-directory"), "classpath", "Main");
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Native directory"));
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

    @Test
    public void launchFailureLeavesManagerInFailedState() throws Exception {
        File root = Files.createTempDirectory("droid-launch-failure").toFile();
        File invalidJava = new File(root, "not-a-java-executable");
        assertTrue(invalidJava.createNewFile());
        File game = new File(root, "game");
        File natives = new File(root, "natives");
        assertTrue(game.mkdirs());
        assertTrue(natives.mkdirs());

        MinecraftLaunchManager manager = new MinecraftLaunchManager();
        try {
            try {
                manager.launch(new JavaRuntime(invalidJava, 17, "test"), game, natives,
                        "classpath.jar", "com.example.Main",
                        Collections.emptyList(), Collections.emptyList(), null);
            } catch (java.io.IOException expected) {
                assertFalse(manager.getState() == LaunchState.RUNNING);
                assertFalse(manager.getLastError().isEmpty());
                assertNull(manager.getProcess());
                assertTrue(manager.getState() == LaunchState.FAILED);
                return;
            }
            throw new AssertionError("Expected process startup to fail");
        } finally {
            manager.shutdown();
        }
    }

    @Test
    public void preflightFailureIsRecordedByManager() throws Exception {
        File root = Files.createTempDirectory("droid-launch-preflight").toFile();
        File invalidJava = new File(root, "missing-java");
        File game = new File(root, "game");
        File natives = new File(root, "natives");
        assertTrue(game.mkdirs());
        assertTrue(natives.mkdirs());

        MinecraftLaunchManager manager = new MinecraftLaunchManager();
        try {
            try {
                manager.launch(new JavaRuntime(invalidJava, 17, "test"), game, natives,
                        "classpath.jar", "com.example.Main",
                        Collections.emptyList(), Collections.emptyList(), null);
            } catch (java.io.IOException expected) {
                assertTrue(manager.getState() == LaunchState.FAILED);
                assertTrue(manager.getLastError().contains("Java runtime"));
                return;
            }
            throw new AssertionError("Expected preflight validation to fail");
        } finally {
            manager.shutdown();
        }
    }
}
