package com.droidlauncher.launcher;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.view.Surface;

/** Minimal Android EGL/OpenGL ES renderer boundary for the gameplay Surface. */
public final class AndroidEglRenderer implements AndroidSurfaceBridge.Consumer {
    private EGLDisplay display = EGL14.EGL_NO_DISPLAY;
    private EGLContext context = EGL14.EGL_NO_CONTEXT;
    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private Surface surface;
    private long generation;
    private int width;
    private int height;

    @Override
    public synchronized void onSurfaceAvailable(Surface surface, int width, int height, long generation) {
        onSurfaceAvailableInternal(surface, width, height, generation);
    }

    public synchronized boolean onSurfaceAvailableInternal(Surface surface, int width, int height, long generation) {
        if (surface == null || !surface.isValid()) return false;
        releaseLocked();
        try {
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (display == EGL14.EGL_NO_DISPLAY) return failLocked();
            int[] version = new int[2];
            if (!EGL14.eglInitialize(display, version, 0, version, 1)) return failLocked();
            int[] configAttributes = {
                    EGL14.EGL_RENDERABLE_TYPE, 4,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_DEPTH_SIZE, 16,
                    EGL14.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!EGL14.eglChooseConfig(display, configAttributes, 0, configs, 0, 1, numConfigs, 0)
                    || numConfigs[0] == 0) return failLocked();
            int[] contextAttributes = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
            context = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT,
                    contextAttributes, 0);
            if (context == EGL14.EGL_NO_CONTEXT) return failLocked();
            eglSurface = EGL14.eglCreateWindowSurface(display, configs[0], surface,
                    new int[]{EGL14.EGL_NONE}, 0);
            if (eglSurface == EGL14.EGL_NO_SURFACE) return failLocked();
            if (!EGL14.eglMakeCurrent(display, eglSurface, eglSurface, context)) return failLocked();
            this.surface = surface;
            this.width = Math.max(1, width);
            this.height = Math.max(1, height);
            this.generation = generation;
            renderFrameLocked();
            return isReadyLocked();
        } catch (RuntimeException error) {
            releaseLocked();
            return false;
        }
    }

    @Override
    public synchronized void onSurfaceSizeChanged(Surface surface, int width, int height, long generation) {
        if (generation < this.generation) return;
        if (surface == null || !surface.isValid()) {
            onSurfaceDestroyed(generation);
            return;
        }
        this.surface = surface;
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.generation = generation;
        if (!isReadyLocked()) {
            onSurfaceAvailableInternal(surface, width, height, generation);
            return;
        }
        if (EGL14.eglMakeCurrent(display, eglSurface, eglSurface, context)) {
            renderFrameLocked();
        }
    }

    @Override
    public synchronized void onSurfaceDestroyed(long generation) {
        if (generation < this.generation) return;
        releaseLocked();
        this.generation = generation;
    }

    public synchronized boolean renderTestFrame() {
        if (!isReadyLocked()) return false;
        return renderFrameLocked();
    }

    public synchronized boolean isReady() { return isReadyLocked(); }
    public synchronized long getGeneration() { return generation; }
    public synchronized int getWidth() { return width; }
    public synchronized int getHeight() { return height; }
    public synchronized void release() { releaseLocked(); }

    private boolean renderFrameLocked() {
        if (!isReadyLocked()) return false;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0.025f, 0.035f, 0.055f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        return EGL14.eglSwapBuffers(display, eglSurface);
    }

    private boolean isReadyLocked() {
        return display != EGL14.EGL_NO_DISPLAY
                && context != EGL14.EGL_NO_CONTEXT
                && eglSurface != EGL14.EGL_NO_SURFACE
                && surface != null
                && surface.isValid();
    }

    private boolean failLocked() {
        releaseLocked();
        return false;
    }

    private void releaseLocked() {
        if (display != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            if (eglSurface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(display, eglSurface);
            if (context != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(display, context);
            EGL14.eglTerminate(display);
        }
        display = EGL14.EGL_NO_DISPLAY;
        context = EGL14.EGL_NO_CONTEXT;
        eglSurface = EGL14.EGL_NO_SURFACE;
        surface = null;
        width = 0;
        height = 0;
    }
}
