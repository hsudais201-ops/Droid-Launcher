package com.droidlauncher.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.droidlauncher.runtime.JavaRuntime;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.Test;

public class MinecraftCommandTest {
    @Test
    public void buildsProcessBuilderCommandWithoutQuotingArguments() throws Exception {
        File root = Files.createTempDirectory("droid command test").toFile();
        File java = new File(root, "runtime/java");
        assertEquals(true, java.getParentFile().mkdirs());
        assertEquals(true, java.createNewFile());
        File gameDir = new File(root, "Minecraft Game");
        assertEquals(true, gameDir.mkdirs());

        MinecraftCommand command = new MinecraftCommand(
                java,
                gameDir,
                Arrays.asList(
                        "-Xmx2048M",
                        "-Djava.library.path=" + new File(gameDir, "native libs").getAbsolutePath(),
                        "-cp",
                        new File(gameDir, "libraries/a.jar").getAbsolutePath() + File.pathSeparator + new File(gameDir, "libraries/b.jar").getAbsolutePath(),
                        "net.minecraft.client.main.Main",
                        "--gameDir",
                        gameDir.getAbsolutePath()));

        assertEquals(java.getAbsolutePath(), command.toProcessBuilderCommand().get(0));
        assertEquals("-Djava.library.path=" + new File(gameDir, "native libs").getAbsolutePath(), command.getArguments().get(1));
        assertEquals("net.minecraft.client.main.Main", command.getArguments().get(4));
        assertEquals(gameDir.getAbsolutePath(), command.getArguments().get(6));
        assertEquals(8, command.toProcessBuilderCommand().size());
    }

    @Test
    public void rejectsNullOrNulArguments() throws Exception {
        File root = Files.createTempDirectory("droid command invalid").toFile();
        File java = new File(root, "java");
        assertEquals(true, java.createNewFile());
        File gameDir = new File(root, "game");
        assertEquals(true, gameDir.mkdirs());

        assertThrows(IllegalArgumentException.class,
                () -> new MinecraftCommand(java, gameDir, Arrays.asList("-Dkey=value\0bad")));
    }

    @Test
    public void launchPreflightAcceptsCompleteInputs() throws Exception {
        Fixture fixture = new Fixture();
        LaunchPreflight.Result result = fixture.preflight.validate(
                fixture.runtime, fixture.gameDirectory, fixture.nativesDirectory,
                "a.jar:b.jar", "net.minecraft.client.main.Main");
        assertTrue(result.isValid());
    }

    @Test
    public void launchPreflightRejectsInvalidInputs() throws Exception {
        Fixture fixture = new Fixture();
        File rootFile = fixture.root.toFile();

        assertFalse(fixture.preflight.validate(
                new JavaRuntime(new File(rootFile, "missing-java"), 17, "17"),
                fixture.gameDirectory, fixture.nativesDirectory, "a.jar", "Main").isValid());
        assertFalse(fixture.preflight.validate(
                fixture.runtime, new File(rootFile, "missing-game"), fixture.nativesDirectory,
                "a.jar", "Main").isValid());
        assertFalse(fixture.preflight.validate(
                fixture.runtime, fixture.gameDirectory, new File(rootFile, "missing-natives"),
                "a.jar", "Main").isValid());
        assertFalse(fixture.preflight.validate(
                fixture.runtime, fixture.gameDirectory, fixture.nativesDirectory, "  ", "Main").isValid());
        assertFalse(fixture.preflight.validate(
                fixture.runtime, fixture.gameDirectory, fixture.nativesDirectory, "a.jar", "  ").isValid());
    }

    private static final class Fixture {
        final Path root;
        final File gameDirectory;
        final File nativesDirectory;
        final JavaRuntime runtime;
        final LaunchPreflight preflight = new LaunchPreflight();

        Fixture() throws Exception {
            root = Files.createTempDirectory("droid-launcher-preflight-");
            gameDirectory = Files.createDirectory(root.resolve("game")).toFile();
            nativesDirectory = Files.createDirectory(root.resolve("natives")).toFile();
            File java = Files.createFile(root.resolve("java")).toFile();
            runtime = new JavaRuntime(java, 17, "17");
        }
    }
}
