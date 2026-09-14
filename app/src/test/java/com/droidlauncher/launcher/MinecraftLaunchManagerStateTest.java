package com.droidlauncher.launcher;

import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

public final class MinecraftLaunchManagerStateTest {
    @Test
    public void startsIdle() {
        MinecraftLaunchManager manager = new MinecraftLaunchManager();
        try {
            assertEquals(LaunchState.IDLE, manager.getState());
            assertEquals(null, manager.getProcess());
            assertEquals("", manager.getLastError());
        } finally {
            manager.shutdown();
        }
    }

    @Test
    public void preflightFailureEndsInFailedState() {
        MinecraftLaunchManager manager = new MinecraftLaunchManager();
        try {
            assertThrows(java.io.IOException.class, () -> manager.launch(
                    null,
                    new File("missing-game"),
                    new File("missing-natives"),
                    "classpath",
                    "net.minecraft.client.main.Main",
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyMap()));
            assertEquals(LaunchState.FAILED, manager.getState());
            assertTrue(manager.getLastError().contains("Java runtime"));
            assertEquals(null, manager.getProcess());
        } finally {
            manager.shutdown();
        }
    }

    @Test
    public void stopWithoutProcessEndsStopped() {
        MinecraftLaunchManager manager = new MinecraftLaunchManager();
        try {
            manager.stop();
            assertEquals(LaunchState.STOPPED, manager.getState());
            assertEquals(null, manager.getProcess());
        } finally {
            manager.shutdown();
        }
    }

    @Test
    public void rejectsNullDependencies() {
        assertThrows(IllegalArgumentException.class,
                () -> new MinecraftLaunchManager(null, new LaunchPreflight()));
        assertThrows(IllegalArgumentException.class,
                () -> new MinecraftLaunchManager(new MinecraftProcessLauncher(), null));
    }
}
