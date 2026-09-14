package com.droidlauncher;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.SurfaceView;
import android.widget.FrameLayout;

/**
 * Reusable gameplay container that stacks a rendering surface with the saved touch overlay.
 * The rendering surface remains independent of the input layer so a future native/GLFW
 * renderer can consume the same surface without changing touch-control code.
 */
public final class MinecraftGameplayInputLayer extends FrameLayout {
    public interface Listener extends TouchControlOverlayView.Listener { }

    private final SurfaceView surfaceView;
    private final TouchControlOverlayView overlayView;
    private final TouchControlInputBridge inputBridge;

    public MinecraftGameplayInputLayer(Context context) {
        super(context);
        setBackgroundColor(Color.BLACK);
        setFocusable(true);
        setFocusableInTouchMode(true);

        surfaceView = new SurfaceView(context);
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

    public SurfaceView getSurfaceView() {
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
