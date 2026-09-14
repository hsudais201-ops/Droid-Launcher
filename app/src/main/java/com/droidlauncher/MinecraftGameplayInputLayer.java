package com.droidlauncher;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.droidlauncher.launcher.MinecraftSurfaceHost;

/**
 * Reusable gameplay container that stacks the Android surface host with the saved touch overlay.
 * The surface lifecycle can be consumed by the future native EGL/GLFW/LWJGL renderer.
 */
public final class MinecraftGameplayInputLayer extends FrameLayout {
    public interface Listener extends TouchControlOverlayView.Listener { }

    private final MinecraftSurfaceHost surfaceView;
    private final TouchControlOverlayView overlayView;
    private final TouchControlInputBridge inputBridge;

    public MinecraftGameplayInputLayer(Context context) {
        super(context);
        setBackgroundColor(Color.BLACK);
        setFocusable(true);
        setFocusableInTouchMode(true);

        surfaceView = new MinecraftSurfaceHost(context);
        LayoutParams surfaceParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
        addView(surfaceView, surfaceParams);

        overlayView = new TouchControlOverlayView(context);
        overlayView.setBackgroundColor(Color.TRANSPARENT);
        LayoutParams overlayParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
        addView(overlayView, overlayParams);

        inputBridge = new TouchControlInputBridge();
        overlayView.setInputSink(new TouchControlInputBridge.Sink() {
            @Override public void onButtonDown(TouchControlAction action) {
                inputBridge.onControlDown(action);
            }

            @Override public void onButtonUp(TouchControlAction action) {
                inputBridge.onControlUp(action);
            }

            @Override public void onAnalogMove(TouchControlAction action, float deltaX, float deltaY) {
                inputBridge.onAnalogMove(action, deltaX, deltaY);
            }
        });
    }

    public MinecraftSurfaceHost getSurfaceView() {
        return surfaceView;
    }

    public TouchControlOverlayView getOverlayView() {
        return overlayView;
    }

    public void setInputSink(TouchControlInputBridge.Sink sink) {
        inputBridge.setSink(sink);
    }

    public void refreshControls() {
        overlayView.refresh();
    }

    public void releaseInputs() {
        overlayView.releaseAllInputs();
        inputBridge.releaseAll();
    }

    @Override
    protected void onDetachedFromWindow() {
        releaseInputs();
        super.onDetachedFromWindow();
    }
}
