package com.droidlauncher.launcher;

import android.view.Surface;

/** Coordinates the Android Surface lifecycle between EGL and the eventual native Minecraft renderer. */
public final class MinecraftRendererCoordinator implements AndroidSurfaceBridge.Consumer {
    private final MinecraftNativeRendererSession session = new MinecraftNativeRendererSession();
    private final AndroidEglRenderer eglRenderer = new AndroidEglRenderer();

    @Override
    public void onSurfaceAvailable(Surface surface, int width, int height, long generation) {
        session.onSurfaceAvailable(surface, width, height, generation);
        eglRenderer.onSurfaceAvailable(surface, width, height, generation);
    }

    @Override
    public void onSurfaceSizeChanged(Surface surface, int width, int height, long generation) {
        session.onSurfaceSizeChanged(surface, width, height, generation);
        eglRenderer.onSurfaceSizeChanged(surface, width, height, generation);
    }

    @Override
    public void onSurfaceDestroyed(long generation) {
        eglRenderer.onSurfaceDestroyed(generation);
        session.onSurfaceDestroyed(generation);
    }

    public boolean renderTestFrame() {
        return eglRenderer.renderTestFrame();
    }

    public boolean isSurfaceReady() {
        return session.isReady() && eglRenderer.isReady();
    }

    public Surface getSurface() {
        return session.getSurface();
    }

    public int getWidth() {
        return session.getWidth();
    }

    public int getHeight() {
        return session.getHeight();
    }

    public long getGeneration() {
        return session.getGeneration();
    }

    public MinecraftNativeRendererSession.State getState() {
        return session.getState();
    }

    public void release() {
        eglRenderer.release();
        session.reset();
    }
}
