package com.ashveil.input;

public class PlayerInput {
    private final float moveX;
    private final float moveY;
    private final boolean primaryActionPressed;
    private final boolean interactPressed;
    private final boolean useItemPressed;
    private final boolean dropItemPressed;
    private final boolean dropWholeStack;
    private final boolean dashPressed;
    private final int selectedHotbarSlot;

    public PlayerInput(float moveX, float moveY, boolean primaryActionPressed, boolean interactPressed,
                       boolean useItemPressed, boolean dropItemPressed, boolean dropWholeStack, boolean dashPressed, int selectedHotbarSlot) {
        this.moveX = moveX;
        this.moveY = moveY;
        this.primaryActionPressed = primaryActionPressed;
        this.interactPressed = interactPressed;
        this.useItemPressed = useItemPressed;
        this.dropItemPressed = dropItemPressed;
        this.dropWholeStack = dropWholeStack;
        this.dashPressed = dashPressed;
        this.selectedHotbarSlot = selectedHotbarSlot;
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
    public boolean isUseItemPressed() {
        return useItemPressed;
    }
    public boolean isDropItemPressed() {
        return dropItemPressed;
    }
    public boolean isDropWholeStack() {
        return dropWholeStack;
    }
    public boolean isDashPressed() {
        return dashPressed;
    }
    public int getSelectedHotbarSlot() {
        return selectedHotbarSlot;
    }
}
