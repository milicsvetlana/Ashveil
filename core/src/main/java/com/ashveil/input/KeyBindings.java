package com.ashveil.input;

import com.badlogic.gdx.Input;
//class exists so in future we will be able to let user change commands
public class KeyBindings {
    private int moveUpKey;
    private int moveDownKey;
    private int moveLeftKey;
    private int moveRightKey;

    private int primaryActionKey;
    private int interactKey;
    private int useItemKey;
    private int dropItemKey;
    private int dropWholeStackModifierKey;
    private int dashKey;

    private final int[] hotbarKeys;

    private int toggleOverlayKey;
    private int pauseMenuKey;

    public KeyBindings() {
        moveUpKey = Input.Keys.W;
        moveDownKey = Input.Keys.S;
        moveLeftKey = Input.Keys.A;
        moveRightKey = Input.Keys.D;

        primaryActionKey = Input.Keys.K;
        interactKey = Input.Keys.E;
        useItemKey = Input.Keys.F;
        dropItemKey = Input.Keys.Q;
        dropWholeStackModifierKey = Input.Keys.CONTROL_LEFT;
        dashKey = Input.Keys.SHIFT_LEFT;

        hotbarKeys = new int[]{
            Input.Keys.NUM_1,
            Input.Keys.NUM_2,
            Input.Keys.NUM_3,
            Input.Keys.NUM_4,
            Input.Keys.NUM_5
        };

        toggleOverlayKey = Input.Keys.TAB;
        pauseMenuKey = Input.Keys.ESCAPE;
    }

    public int getMoveUpKey() {
        return moveUpKey;
    }

    public int getMoveDownKey() {
        return moveDownKey;
    }

    public int getMoveLeftKey() {
        return moveLeftKey;
    }

    public int getMoveRightKey() {
        return moveRightKey;
    }

    public int getPrimaryActionKey() {
        return primaryActionKey;
    }

    public int getInteractKey() {
        return interactKey;
    }

    public int getUseItemKey() {
        return useItemKey;
    }

    public int getDropItemKey() {
        return dropItemKey;
    }

    public int getDropWholeStackModifierKey() {
        return dropWholeStackModifierKey;
    }

    public int getDashKey() {
        return dashKey;
    }

    public int getHotbarKey(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= hotbarKeys.length) {
            return Input.Keys.UNKNOWN;
        }

        return hotbarKeys[slotIndex];
    }

    public int getHotbarSize() {
        return hotbarKeys.length;
    }

    public int getToggleOverlayKey() {
        return toggleOverlayKey;
    }

    public int getPauseMenuKey() {
        return pauseMenuKey;
    }
}
