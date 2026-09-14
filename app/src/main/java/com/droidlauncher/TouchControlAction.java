package com.droidlauncher;

public enum TouchControlAction {
    FORWARD("Forward"), BACKWARD("Backward"), LEFT("Left"), RIGHT("Right"),
    JOYSTICK("Movement Joystick"), JUMP("Jump"), SNEAK("Sneak"), SPRINT("Sprint"),
    ATTACK("Attack"), USE_ITEM("Use Item"), DROP("Drop"), INVENTORY("Inventory"),
    CHAT("Chat"), PAUSE("Pause"), CAMERA("Camera / Mouse");

    private final String label;

    TouchControlAction(String label) { this.label = label; }
    public String getLabel() { return label; }
}
