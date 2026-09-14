package com.droidlauncher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight gameplay overlay for the saved touch-control layout.
 * Button events and analog movement are routed through the input bridge seam.
 */
public final class TouchControlOverlayView extends View {
    public interface Listener {
        void onControlDown(TouchControlAction action);
        void onControlUp(TouchControlAction action);
    }

    private final TouchControlStore store;
    private final TouchControlInputBridge inputBridge = new TouchControlInputBridge();
    private final List<TouchControlConfig> controls = new ArrayList<>();
    private final Map<Integer, TouchControlAction> activeActions = new HashMap<>();
    private final Map<Integer, Float> lastX = new HashMap<>();
    private final Map<Integer, Float> lastY = new HashMap<>();
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Listener listener;

    public TouchControlOverlayView(Context context) {
        super(context);
        store = new TouchControlStore(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        fill.setColor(Color.argb(110, 60, 90, 120));
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(2f);
        stroke.setColor(Color.argb(175, 220, 235, 250));
        text.setColor(Color.WHITE);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(28f);
        refresh();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** Connects the overlay to a future native/GLFW game-input consumer. */
    public void setInputSink(TouchControlInputBridge.Sink sink) {
        inputBridge.setSink(sink);
    }

    /** Releases all currently pressed controls, useful when the game surface is paused. */
    public void releaseAllInputs() {
        activeActions.clear();
        lastX.clear();
        lastY.clear();
        inputBridge.releaseAll();
    }

    public void refresh() {
        controls.clear();
        controls.addAll(store.load());
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        for (TouchControlConfig config : controls) {
            if (!config.visible) continue;
            float left = clamp(config.x * w, 0f, Math.max(0f, w - config.width * w));
            float top = clamp(config.y * h, 0f, Math.max(0f, h - config.height * h));
            float right = Math.min(w, left + Math.max(54f, config.width * w));
            float bottom = Math.min(h, top + Math.max(44f, config.height * h));
            fill.setAlpha(Math.round(255f * clamp(config.opacity, 0.15f, 1f) * 0.55f));
            stroke.setAlpha(Math.round(255f * clamp(config.opacity, 0.15f, 1f)));
            RectF rect = new RectF(left, top, right, bottom);
            float radius = "circle".equals(config.shape) ? Math.min(rect.width(), rect.height()) / 2f
                    : ("rounded".equals(config.shape) ? 20f : 4f);
            canvas.drawRoundRect(rect, radius, radius, fill);
            canvas.drawRoundRect(rect, radius, radius, stroke);
            float baseline = top + rect.height() / 2f - (text.ascent() + text.descent()) / 2f;
            text.setTextSize(Math.max(12f, Math.min(28f, rect.height() * 0.28f)));
            canvas.drawText(config.label, left + rect.width() / 2f, baseline, text);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int masked = event.getActionMasked();
        if (masked != MotionEvent.ACTION_DOWN
                && masked != MotionEvent.ACTION_POINTER_DOWN
                && masked != MotionEvent.ACTION_MOVE
                && masked != MotionEvent.ACTION_UP
                && masked != MotionEvent.ACTION_POINTER_UP
                && masked != MotionEvent.ACTION_CANCEL) {
            return true;
        }

        if (masked == MotionEvent.ACTION_MOVE) {
            handleMoves(event);
            return true;
        }

        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        if (masked == MotionEvent.ACTION_CANCEL) {
            releaseAllInputs();
            return true;
        }

        if (masked == MotionEvent.ACTION_DOWN || masked == MotionEvent.ACTION_POINTER_DOWN) {
            TouchControlConfig hit = hitTest(x, y);
            if (hit == null) return true;
            activeActions.put(pointerId, hit.action);
            lastX.put(pointerId, x);
            lastY.put(pointerId, y);
            inputBridge.onControlDown(hit.action);
            if (listener != null) listener.onControlDown(hit.action);
            return true;
        }

        TouchControlAction action = activeActions.remove(pointerId);
        lastX.remove(pointerId);
        lastY.remove(pointerId);
        if (action != null) {
            inputBridge.onControlUp(action);
            if (listener != null) listener.onControlUp(action);
        }
        return true;
    }

    private void handleMoves(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            TouchControlAction action = activeActions.get(pointerId);
            if (action == null) continue;
            float x = event.getX(i);
            float y = event.getY(i);
            Float previousX = lastX.put(pointerId, x);
            Float previousY = lastY.put(pointerId, y);
            if (previousX == null || previousY == null) continue;
            if (action == TouchControlAction.CAMERA || action == TouchControlAction.JOYSTICK) {
                float dx = (x - previousX) / Math.max(1f, getWidth()) * 4f;
                float dy = (y - previousY) / Math.max(1f, getHeight()) * 4f;
                inputBridge.onAnalogMove(action, dx, dy);
            }
        }
    }

    private TouchControlConfig hitTest(float x, float y) {
        float w = getWidth();
        float h = getHeight();
        for (int i = controls.size() - 1; i >= 0; i--) {
            TouchControlConfig config = controls.get(i);
            if (!config.visible) continue;
            float left = clamp(config.x * w, 0f, Math.max(0f, w - config.width * w));
            float top = clamp(config.y * h, 0f, Math.max(0f, h - config.height * h));
            float right = Math.min(w, left + Math.max(54f, config.width * w));
            float bottom = Math.min(h, top + Math.max(44f, config.height * h));
            if (x >= left && x <= right && y >= top && y <= bottom) return config;
        }
        return null;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
