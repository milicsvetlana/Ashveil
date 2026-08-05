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
    private final Drawable defaultBackground;
    private final Drawable selectedBackground;

    public InventorySlotUi(int slotIndex, boolean hotbarSlot, Skin skin){
        this.slotIndex = slotIndex;

        String temp = hotbarSlot ? "hotbar-slot" : "inventory-slot";
        defaultBackground = skin.getDrawable(temp);
        selectedBackground = skin.getDrawable("inventory-slot-selected");
        backgroundImage = new Image(defaultBackground);


        itemImage = new Image(skin.getDrawable("item-placeholder"));
        itemImage.setScaling(Scaling.fit);
        itemImage.setVisible(false);

        quantityLabel = new Label("", skin);

        Table quantityOverlay = new Table();
        quantityOverlay.bottom().right();
        quantityOverlay.add(quantityLabel).pad(4);

        Table itemLayer = new Table();
        itemLayer.add(itemImage).grow().pad(10);

        add(backgroundImage);
        add(itemLayer);
        add(quantityOverlay);

        backgroundImage.setTouchable(Touchable.disabled);
        itemImage.setTouchable(Touchable.disabled);
        itemLayer.setTouchable(Touchable.disabled);
        quantityOverlay.setTouchable(Touchable.disabled);

        setTouchable(Touchable.enabled); //omogucava da klik pripada celom bloku a ne slici ili tabeli
    }

    public void refresh(ItemStack itemStack){
        if (itemStack == null) {
            itemImage.setVisible(false);
            quantityLabel.setText("");
            return;
        }
        itemImage.setVisible(true);
        itemImage.setColor(getTemporaryColor(itemStack.getType()));

        if (itemStack.getQuantity() > 1) quantityLabel.setText(String.valueOf(itemStack.getQuantity()));
        else quantityLabel.setText("");
    }

    private Color getTemporaryColor(ItemType type){
        return switch (type) {
            case WOOD -> new Color(0.45f, 0.25f, 0.1f, 1f);
            case STONE -> Color.GRAY;

            case WHEAT, BREAD ->
                new Color(0.9f, 0.75f, 0.2f, 1f);

            case WHEAT_SEED ->
                new Color(0.2f, 0.65f, 0.2f, 1f);

            case WOODEN_AXE, WOODEN_PICKAXE, WOODEN_HOE, WOODEN_SWORD ->
                new Color(0.65f, 0.3f, 0.15f, 1f);

            case STONE_AXE, STONE_PICKAXE, STONE_HOE, STONE_SWORD ->
                new Color(0.55f, 0.55f, 0.6f, 1f);

            case FENCE ->
                new Color(0.5f, 0.3f, 0.12f, 1f);

            case BOAT_KIT ->
                new Color(0.35f, 0.55f, 0.7f, 1f);

            case LORE_SCROLL ->
                new Color(0.65f, 0.4f, 0.75f, 1f);
        };
    }

    public void setSelected(boolean selected){
        backgroundImage.setDrawable(selected ? selectedBackground : defaultBackground);
    }

    public int getSlotIndex() {return slotIndex;}
}
