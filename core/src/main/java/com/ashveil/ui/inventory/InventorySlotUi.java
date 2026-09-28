package com.ashveil.ui.inventory;

import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.inventory.ItemType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;

public class InventorySlotUi extends Stack {

    private final int slotIndex;

    private final Image backgroundImage;
    private final Image itemImage;
    private final Label quantityLabel;
    private final DurabilityBarUi durabilityBar;

    private final Drawable defaultBackground;
    private final Drawable selectedBackground;
    private final Drawable keyboardPickedUpBackground;

    private boolean selected;
    private boolean keyboardPickedUp;
    private final Skin skin;

    public InventorySlotUi(int slotIndex, boolean hotbarSlot, Skin skin) {
        this.slotIndex = slotIndex;
        this.skin = skin;

        String backgroundName = hotbarSlot ? "hotbar-slot" : "inventory-slot";

        defaultBackground = skin.getDrawable(backgroundName);
        selectedBackground = skin.getDrawable("inventory-slot-selected");
        keyboardPickedUpBackground = skin.getDrawable("inventory-slot-picked");

        backgroundImage = new Image(defaultBackground);
        itemImage = createItemImage(skin);
        quantityLabel = new Label("", skin);
        durabilityBar = new DurabilityBarUi(skin);

        Table itemLayer = createItemLayer();
        Table quantityOverlay = createQuantityOverlay();

        add(backgroundImage);
        add(itemLayer);
        add(durabilityBar);
        add(quantityOverlay);

        disableChildTouch(itemLayer, quantityOverlay);
        setTouchable(Touchable.enabled);
    }

    private Image createItemImage(Skin skin) {
        Image image = new Image(skin.getDrawable("item-placeholder"));

        image.setScaling(Scaling.fit);
        image.setVisible(false);
        image.setTouchable(Touchable.disabled);

        return image;
    }

    private Table createItemLayer() {
        Table itemLayer = new Table();
        itemLayer.add(itemImage).grow().pad(8);
        return itemLayer;
    }

    private Table createQuantityOverlay() {
        Table quantityOverlay = new Table();

        quantityOverlay.bottom().right();
        quantityOverlay.add(quantityLabel).pad(4);

        return quantityOverlay;
    }

    private void disableChildTouch(
        Table itemLayer,
        Table quantityOverlay
    ) {
        backgroundImage.setTouchable(Touchable.disabled);
        itemLayer.setTouchable(Touchable.disabled);
        quantityOverlay.setTouchable(Touchable.disabled);
        quantityLabel.setTouchable(Touchable.disabled);
    }

    public void refresh(ItemStack itemStack) {
        if (itemStack == null) {
            clearSlot();
            return;
        }

        showItem(itemStack);
        updateQuantity(itemStack);
        updateDurability(itemStack);
    }

    private void clearSlot() {
        itemImage.setVisible(false);
        quantityLabel.setText("");
        durabilityBar.clear();
    }

    private void showItem(ItemStack itemStack) {
        itemImage.setDrawable(
            ItemIconUi.getDrawable(skin, itemStack.getType())
        );

        itemImage.setColor(Color.WHITE);
        itemImage.setVisible(true);
    }

    private void updateQuantity(ItemStack itemStack) {
        if (itemStack.getQuantity() > 1) quantityLabel.setText(String.valueOf(itemStack.getQuantity()));
        else quantityLabel.setText("");
    }

    private void updateDurability(ItemStack itemStack) {
        ItemType type = itemStack.getType();

        if (!type.usesDurability()) {
            durabilityBar.clear();
            return;
        }

        durabilityBar.setDurability(itemStack.getDurability(), type.getMaxDurability());
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateBackground();
    }

    public void setKeyboardPickedUp(boolean keyboardPickedUp) {
        this.keyboardPickedUp = keyboardPickedUp;
        updateBackground();
    }

    private void updateBackground() {
        if (keyboardPickedUp) {
            backgroundImage.setDrawable(keyboardPickedUpBackground);
            return;
        }

        if (selected) {
            backgroundImage.setDrawable(selectedBackground);
            return;
        }

        backgroundImage.setDrawable(defaultBackground);
    }

    public int getSlotIndex() {
        return slotIndex;
    }
}
