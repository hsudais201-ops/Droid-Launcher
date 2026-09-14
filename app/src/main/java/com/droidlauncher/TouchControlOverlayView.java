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
    private final Map<Integer, TouchControlConfig> activeControls = new HashMap<>();
    private final Map<Integer, Float> lastX = new HashMap<>();
    private final Map<Integer, Float> lastY = new HashMap<>();
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint analogFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint analogStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
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
        analogFill.setColor(Color.argb(90, 220, 235, 250));
        analogStroke.setStyle(Paint.Style.STROKE);
        analogStroke.setStrokeWidth(2f);
        analogStroke.setColor(Color.argb(220, 230, 245, 255));
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
        activeControls.clear();
        lastX.clear();
        lastY.clear();
        inputBridge.releaseAll();
        invalidate();
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
            RectF rect = bounds(config, w, h);
            float alpha = clamp(config.opacity, 0.15f, 1f);
            fill.setAlpha(Math.round(255f * alpha * 0.55f));
            stroke.setAlpha(Math.round(255f * alpha));
            float radius = "circle".equals(config.shape) ? Math.min(rect.width(), rect.height()) / 2f
                    : ("rounded".equals(config.shape) ? 20f : 4f);
            canvas.drawRoundRect(rect, radius, radius, fill);
            canvas.drawRoundRect(rect, radius, radius, stroke);
            if (config.action == TouchControlAction.JOYSTICK) {
                drawJoystick(canvas, config, rect, activePointerFor(config));
                continue;
            }
            float baseline = rect.centerY() - (text.ascent() + text.descent()) / 2f;
            text.setTextSize(Math.max(12f, Math.min(28f, rect.height() * 0.28f)));
            canvas.drawText(config.label, rect.centerX(), baseline, text);
            if (config.action == TouchControlAction.CAMERA) {
                canvas.drawCircle(rect.centerX(), rect.centerY(), Math.min(rect.width(), rect.height()) * 0.08f, analogStroke);
            }
        }
    }

    private void drawJoystick(Canvas canvas, TouchControlConfig config, RectF rect, Integer pointerId) {
        float cx = rect.centerX();
        float cy = rect.centerY();
        float outer = Math.min(rect.width(), rect.height()) * 0.44f;
        canvas.drawCircle(cx, cy, outer, analogStroke);
        float knobX = cx;
        float knobY = cy;
        if (pointerId != null && lastX.containsKey(pointerId) && lastY.containsKey(pointerId)) {
            float radius = outer * 0.68f;
            float dx = lastX.get(pointerId) - cx;
            float dy = lastY.get(pointerId) - cy;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance > radius && distance > 0f) {
                float scale = radius / distance;
                dx *= scale;
                dy *= scale;
            }
            knobX += dx;
            knobY += dy;
        }
        canvas.drawCircle(knobX, knobY, Math.max(12f, outer * 0.34f), analogFill);
        text.setTextSize(Math.max(12f, Math.min(24f, rect.height() * 0.2f)));
        float baseline = rect.centerY() - (text.ascent() + text.descent()) / 2f;
        canvas.drawText(config.label, rect.centerX(), baseline, text);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int masked = event.getActionMasked();
        if (masked != MotionEvent.ACTION_DOWN
                && masked != MotionEvent.ACTION_POINTER_DOWN
                && masked != MotionEvent.ACTION_MOVE
                && masked != MotionEvent.ACTION_UP
                && masked != MotionEvent.ACTION_POINTER_UP
                && masked != MotionEvent.ACTION_CANCEL) return true;

        if (masked == MotionEvent.ACTION_MOVE) {
            handleMoves(event);
            return true;
        }

        if (masked == MotionEvent.ACTION_CANCEL) {
            releaseAllInputs();
            return true;
        }

        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        if (masked == MotionEvent.ACTION_DOWN || masked == MotionEvent.ACTION_POINTER_DOWN) {
            TouchControlConfig hit = hitTest(x, y);
            if (hit == null) return true;
            activeActions.put(pointerId, hit.action);
            activeControls.put(pointerId, hit);
            lastX.put(pointerId, x);
            lastY.put(pointerId, y);
            inputBridge.onControlDown(hit.action);
            if (listener != null) listener.onControlDown(hit.action);
            if (hit.action == TouchControlAction.JOYSTICK) emitJoystick(hit, pointerId, x, y);
            invalidate();
            return true;
        }

        TouchControlAction action = activeActions.remove(pointerId);
        TouchControlConfig config = activeControls.remove(pointerId);
        lastX.remove(pointerId);
        lastY.remove(pointerId);
        if (action != null) {
            if (action == TouchControlAction.JOYSTICK) inputBridge.onAnalogMove(action, 0f, 0f);
            inputBridge.onControlUp(action);
            if (listener != null) listener.onControlUp(action);
        }
        invalidate();
        return true;
    }

    private void handleMoves(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerId = event.getPointerId(i);
            TouchControlAction action = activeActions.get(pointerId);
            TouchControlConfig config = activeControls.get(pointerId);
            if (action == null || config == null) continue;
            float x = event.getX(i);
            float y = event.getY(i);
            lastX.put(pointerId, x);
            lastY.put(pointerId, y);
            if (action == TouchControlAction.JOYSTICK) {
                emitJoystick(config, pointerId, x, y);
            } else if (action == TouchControlAction.CAMERA) {
                float dx = (x - previous(lastX, pointerId, x)) / Math.max(1f, getWidth()) * 6f;
                float dy = (y - previous(lastY, pointerId, y)) / Math.max(1f, getHeight()) * 6f;
                inputBridge.onAnalogMove(action, dx, dy);
            }
        }
        invalidate();
    }

    private float previous(Map<Integer, Float> values, int pointerId, float current) {
        Float value = values.get(pointerId);
        return value == null ? current : value;
    }

    private void emitJoystick(TouchControlConfig config, int pointerId, float x, float y) {
        RectF rect = bounds(config, getWidth(), getHeight());
        float radius = Math.min(rect.width(), rect.height()) * 0.34f;
        float dx = x - rect.centerX();
        float dy = y - rect.centerY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > radius && distance > 0f) {
            float scale = radius / distance;
            dx *= scale;
            dy *= scale;
        }
        inputBridge.onAnalogMove(TouchControlAction.JOYSTICK,
                clamp(dx / Math.max(1f, radius), -1f, 1f),
                clamp(dy / Math.max(1f, radius), -1f, 1f));
    }

    private TouchControlConfig hitTest(float x, float y) {
        float w = getWidth();
        float h = getHeight();
        for (int i = controls.size() - 1; i >= 0; i--) {
            TouchControlConfig config = controls.get(i);
            if (!config.visible) continue;
            RectF rect = bounds(config, w, h);
            if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) return config;
        }
        return null;
    }

    private RectF bounds(TouchControlConfig config, float w, float h) {
        float cw = Math.max(54f, config.width * w);
        float ch = Math.max(44f, config.height * h);
        float left = clamp(config.x * w, 0f, Math.max(0f, w - cw));
        float top = clamp(config.y * h, 0f, Math.max(0f, h - ch));
        return new RectF(left, top, Math.min(w, left + cw), Math.min(h, top + ch));
    }

    private Integer activePointerFor(TouchControlConfig config) {
        for (Map.Entry<Integer, TouchControlConfig> entry : activeControls.entrySet()) {
            if (entry.getValue() == config) return entry.getKey();
        }
        return null;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
