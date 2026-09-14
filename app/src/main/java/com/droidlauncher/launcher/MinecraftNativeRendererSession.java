package com.droidlauncher.launcher;

import android.view.Surface;

/** Lifecycle/session state shared by Android surface and the future Minecraft native renderer. */
public final class MinecraftNativeRendererSession implements AndroidSurfaceBridge.Consumer {
    public enum State { DETACHED, READY, RESIZED, DESTROYED }

    private Surface surface;
    private int width;
    private int height;
    private long generation;
    private State state = State.DETACHED;

    @Override
    public synchronized void onSurfaceAvailable(Surface surface, int width, int height, long generation) {
        if (surface == null || !surface.isValid()) {
            clearLocked(generation, State.DETACHED);
            return;
        }
        this.surface = surface;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        this.generation = generation;
        this.state = State.READY;
    }

    @Override
    public synchronized void onSurfaceSizeChanged(Surface surface, int width, int height, long generation) {
        if (generation < this.generation) return;
        if (surface == null || !surface.isValid()) {
            clearLocked(generation, State.DESTROYED);
            return;
        }
        this.surface = surface;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        this.generation = generation;
        this.state = State.RESIZED;
    }

    @Override
    public synchronized void onSurfaceDestroyed(long generation) {
        if (generation < this.generation) return;
        clearLocked(generation, State.DESTROYED);
    }

    public synchronized Surface getSurface() { return surface; }
    public synchronized boolean isReady() { return state == State.READY || state == State.RESIZED; }
    public synchronized int getWidth() { return width; }
    public synchronized int getHeight() { return height; }
    public synchronized long getGeneration() { return generation; }
    public synchronized State getState() { return state; }

    public synchronized void reset() {
        clearLocked(0L, State.DETACHED);
    }

    private void clearLocked(long generation, State state) {
        surface = null;
        width = 0;
        height = 0;
        this.generation = generation;
        this.state = state;
    }
}
