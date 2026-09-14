package com.droidlauncher;

public final class TouchControlConfig {
    public String id;
    public String label;
    public TouchControlAction action;
    public float x;
    public float y;
    public float width;
    public float height;
    public float opacity;
    public String shape;
    public boolean visible;

    public TouchControlConfig(String id, String label, TouchControlAction action,
                              float x, float y, float width, float height,
                              float opacity, String shape, boolean visible) {
        this.id = id;
        this.label = label;
        this.action = action;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.opacity = opacity;
        this.shape = shape;
        this.visible = visible;
    }

    public TouchControlConfig copy() {
        return new TouchControlConfig(id, label, action, x, y, width, height, opacity, shape, visible);
    }
}
