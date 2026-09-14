package com.droidlauncher;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Converts overlay control events into a stable game-input contract.
 * A future native/GLFW consumer can implement Sink without changing the UI layer.
 */
public final class TouchControlInputBridge implements TouchControlOverlayView.Listener {
    public interface Sink {
        void onButtonDown(TouchControlAction action);
        void onButtonUp(TouchControlAction action);
        void onAnalogMove(TouchControlAction action, float deltaX, float deltaY);
    }

    private final EnumSet<TouchControlAction> pressed = EnumSet.noneOf(TouchControlAction.class);
    private Sink sink;

    public synchronized void setSink(Sink sink) {
        this.sink = sink;
    }

    public synchronized boolean isPressed(TouchControlAction action) {
        return pressed.contains(action);
    }

    public synchronized Set<TouchControlAction> getPressedActions() {
        return Collections.unmodifiableSet(EnumSet.copyOf(pressed));
    }

    @Override
    public synchronized void onControlDown(TouchControlAction action) {
        if (action == null) return;
        pressed.add(action);
        if (sink != null) sink.onButtonDown(action);
    }

    @Override
    public synchronized void onControlUp(TouchControlAction action) {
        if (action == null) return;
        pressed.remove(action);
        if (sink != null) sink.onButtonUp(action);
    }

    public synchronized void onAnalogMove(TouchControlAction action, float deltaX, float deltaY) {
        if (action == null || sink == null) return;
        sink.onAnalogMove(action, clamp(deltaX), clamp(deltaY));
    }

    public synchronized void releaseAll() {
        if (pressed.isEmpty()) return;
        EnumSet<TouchControlAction> snapshot = EnumSet.copyOf(pressed);
        pressed.clear();
        if (sink != null) {
            for (TouchControlAction action : snapshot) sink.onButtonUp(action);
        }
    }

    private float clamp(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return 0f;
        return Math.max(-1f, Math.min(1f, value));
    }
}
