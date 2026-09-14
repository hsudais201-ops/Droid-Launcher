package com.droidlauncher.launcher;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Owns the Android rendering surface lifecycle used by the future Minecraft/GLFW native host.
 *
 * This class deliberately does not pretend that an Android Surface is a GLFW window. A native
 * layer must consume the Surface and create/configure the actual EGL/GLFW/LWJGL context.
 */
public final class MinecraftSurfaceHost extends SurfaceView implements SurfaceHolder.Callback {
    public interface Listener {
        void onSurfaceAvailable(Surface surface, int width, int height);
        void onSurfaceSizeChanged(Surface surface, int width, int height);
        void onSurfaceDestroyed();
    }

    private Listener listener;
    private Surface surface;
    private int width;
    private int height;

    public MinecraftSurfaceHost(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
        if (surface != null && surface.isValid() && listener != null) {
            listener.onSurfaceAvailable(surface, width, height);
        }
    }

    public Surface getSurface() {
        return surface;
    }

    public boolean isSurfaceReady() {
        return surface != null && surface.isValid();
    }

    public int getSurfaceWidth() {
        return width;
    }

    public int getSurfaceHeight() {
        return height;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surface = holder.getSurface();
        if (listener != null && surface != null) {
            listener.onSurfaceAvailable(surface, width, height);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.width = width;
        this.height = height;
        surface = holder.getSurface();
        if (listener != null && surface != null) {
            listener.onSurfaceSizeChanged(surface, width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surface = null;
        width = 0;
        height = 0;
        if (listener != null) {
            listener.onSurfaceDestroyed();
        }
    }
}
