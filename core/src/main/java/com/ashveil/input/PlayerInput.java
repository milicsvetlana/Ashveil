package com.ashveil.input;

public class PlayerInput {
    private final float moveX;
    private final float moveY;
    private final boolean primaryActionPressed;
    private final boolean interactPressed;

    public PlayerInput(float moveX, float moveY, boolean primary, boolean secondary) {
        this.moveX = moveX;
        this.moveY = moveY;
        this.primaryActionPressed = primary;
        this.interactPressed = secondary;
    }

    public float getMoveX() {
        return moveX;
    }

    public float getMoveY() {
        return moveY;
    }

    public boolean isPrimaryActionPressed() {
        return primaryActionPressed;
    }

    public boolean isInteractPressed() {
        return interactPressed;
    }
}
