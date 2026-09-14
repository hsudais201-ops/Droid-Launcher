package com.droidlauncher.launcher;

import android.view.Surface;

/**
 * Lifecycle-safe handoff object for the future Android EGL/GLFW/LWJGL native runtime.
 *
 * The bridge keeps the latest valid Surface and dimensions and uses a generation counter so a
 * native consumer can reject stale resize/destroy notifications. It does not claim that a Surface
 * is itself a GLFW window; native code must explicitly consume the Surface through an Android/EGL
 * integration layer.
 */
public final class AndroidSurfaceBridge implements MinecraftSurfaceHost.Listener {
    public interface Consumer {
        void onSurfaceAvailable(Surface surface, int width, int height, long generation);
        void onSurfaceSizeChanged(Surface surface, int width, int height, long generation);
        void onSurfaceDestroyed(long generation);
    }

    private final Object lock = new Object();
    private Consumer consumer;
    private Surface surface;
    private int width;
    private int height;
    private long generation;

    public void setConsumer(Consumer consumer) {
        Surface currentSurface;
        int currentWidth;
        int currentHeight;
        long currentGeneration;
        synchronized (lock) {
            this.consumer = consumer;
            currentSurface = surface;
            currentWidth = width;
            currentHeight = height;
            currentGeneration = generation;
        }
        if (consumer != null && currentSurface != null && currentSurface.isValid()) {
            consumer.onSurfaceAvailable(currentSurface, currentWidth, currentHeight, currentGeneration);
        }
    }

    public boolean isReady() {
        synchronized (lock) {
            return surface != null && surface.isValid();
        }
    }

    public long getGeneration() {
        synchronized (lock) {
            return generation;
        }
    }

    public Surface getSurface() {
        synchronized (lock) {
            return surface;
        }
    }

    @Override
    public void onSurfaceAvailable(Surface surface, int width, int height) {
        Consumer currentConsumer;
        long currentGeneration;
        synchronized (lock) {
            this.surface = surface;
            this.width = Math.max(0, width);
            this.height = Math.max(0, height);
            currentGeneration = ++generation;
            currentConsumer = consumer;
        }
        if (currentConsumer != null && surface != null && surface.isValid()) {
            currentConsumer.onSurfaceAvailable(surface, this.width, this.height, currentGeneration);
        }
    }

    @Override
    public void onSurfaceSizeChanged(Surface surface, int width, int height) {
        Consumer currentConsumer;
        long currentGeneration;
        synchronized (lock) {
            this.surface = surface;
            this.width = Math.max(0, width);
            this.height = Math.max(0, height);
            currentGeneration = ++generation;
            currentConsumer = consumer;
        }
        if (currentConsumer != null && surface != null && surface.isValid()) {
            currentConsumer.onSurfaceSizeChanged(surface, this.width, this.height, currentGeneration);
        }
    }

    @Override
    public void onSurfaceDestroyed() {
        Consumer currentConsumer;
        long currentGeneration;
        synchronized (lock) {
            surface = null;
            width = 0;
            height = 0;
            currentGeneration = ++generation;
            currentConsumer = consumer;
        }
        if (currentConsumer != null) {
            currentConsumer.onSurfaceDestroyed(currentGeneration);
        }
    }
}
