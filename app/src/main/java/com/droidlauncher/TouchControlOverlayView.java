package com.droidlauncher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight gameplay overlay for the saved touch-control layout.
 * The listener is the seam for the future native/GLFW input bridge.
 */
public final class TouchControlOverlayView extends View {
    public interface Listener {
        void onControlDown(TouchControlAction action);
        void onControlUp(TouchControlAction action);
    }

    private final TouchControlStore store;
    private final List<TouchControlConfig> controls = new ArrayList<>();
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
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN
                && event.getActionMasked() != MotionEvent.ACTION_POINTER_DOWN
                && event.getActionMasked() != MotionEvent.ACTION_UP
                && event.getActionMasked() != MotionEvent.ACTION_POINTER_UP
                && event.getActionMasked() != MotionEvent.ACTION_CANCEL) {
            return true;
        }
        int pointerIndex = event.getActionIndex();
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        TouchControlConfig hit = hitTest(x, y);
        if (hit == null || listener == null) return true;
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            listener.onControlDown(hit.action);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP
                || action == MotionEvent.ACTION_CANCEL) {
            listener.onControlUp(hit.action);
        }
        return true;
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
