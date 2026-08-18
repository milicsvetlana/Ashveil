package com.ashveil.ui.panels;

import com.ashveil.Config;
import com.ashveil.combat.HitCategory;
import com.ashveil.items.inventory.Inventory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.ui.inventory.InventoryGridUi;
import com.ashveil.ui.inventory.InventorySlotUi;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

//prikazuje svih 20 slotova
public class InventoryPanel extends MenuPanel {

    private final Inventory inventory;
    private int selectedSlotIndex = Config.SLOT_NOT_SELECTED;

    private final InventoryGridUi mainGrid;
    private final InventoryGridUi hotbarGrid;
    private final Table detailsTable;
    private final DragAndDrop dragAndDrop;
    private int keyboardMoveSourceIndex = Config.SLOT_NOT_SELECTED;

    public InventoryPanel(Skin skin, Inventory inventory) {
        super(skin);
        this.inventory = inventory;

        detailsTable = new Table();
        dragAndDrop = new DragAndDrop();
        dragAndDrop.setDragTime(0);
        dragAndDrop.setDragActorPosition(28f, -28f);
        mainGrid = new InventoryGridUi(skin, inventory, Config.HOTBAR_SIZE, inventory.getSize() - Config.HOTBAR_SIZE, Config.HOTBAR_SIZE, false, dragAndDrop);
        hotbarGrid = new InventoryGridUi(skin, inventory, 0, Config.HOTBAR_SIZE, Config.HOTBAR_SIZE, true, dragAndDrop);

        registerSelectionListeners();
        createLayout(skin);
        refresh();
    }

    @Override
    public void refresh(){
        mainGrid.refresh();
        hotbarGrid.refresh();
        if (selectedSlotIndex != Config.SLOT_NOT_SELECTED) {
            updateDetails();
        }
    }

    private void createLayout(Skin skin){
        setBackground(getSkin().getDrawable("inventory-panel-background"));
        pad(20);

        Table inventorySection = new Table();

        inventorySection.add(new Label("Inventory", skin));
        inventorySection.row();
        inventorySection.add(mainGrid);
        inventorySection.row();
        inventorySection.add(new Label("Hotbar", skin));
        inventorySection.row();
        inventorySection.add(hotbarGrid);

        detailsTable.top().left();
        detailsTable.add(new Label("Empty slot.", skin));
        add(inventorySection).grow();
        add(detailsTable).width(260).growY().padLeft(30);
    }

    private void registerSelectionListeners() {
        for (int i = 0; i < inventory.getSize(); i++) {
            int slotIndex = i;
            InventorySlotUi slotUi = getSlotView(slotIndex);

            slotUi.addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    selectSlot(slotIndex);
                }
            });
        }
    }

    private void selectSlot(int slotIndex){
        if (selectedSlotIndex == slotIndex) return;
        if (slotIndex < 0 || slotIndex >= Config.INVENTORY_SIZE) return;
        if (selectedSlotIndex != Config.SLOT_NOT_SELECTED) getSlotView(selectedSlotIndex).setSelected(false);
        selectedSlotIndex = slotIndex;
        getSlotView(selectedSlotIndex).setSelected(true);
        updateDetails();
    }

    private void updateDetails(){
        detailsTable.clearChildren();
        ItemStack item = inventory.getSlot(selectedSlotIndex);
        if (item == null) {
            detailsTable.add(new Label("Empty slot.", getSkin()));
            return;
        }
        detailsTable.add(new Label("Name: " + item.getType().getDisplayName(), getSkin())).left();
        detailsTable.row();
        detailsTable.add(new Label("Description: " + item.getType().getDescription(), getSkin())).padTop(20).left();
        detailsTable.row();
        if (item.getType().usesDurability()) detailsTable.add(new Label("Durability: " + item.getDurability() + " / " + item.getType().getMaxDurability(), getSkin())).left();
        detailsTable.row();
        if (item.getType().isStackable()) detailsTable.add(new Label("Quantity: " + item.getQuantity(), getSkin())).left();
        detailsTable.row();
        detailsTable.add(new Label("Damage: " + item.getType().getDamageProfile().getDamage(HitCategory.ENTITY), getSkin())).left();
    }

    @Override
    public void onHide(){
        clearKeyboardMove();
        clearSelection();
    }

    private void clearSelection() {
        if (selectedSlotIndex != Config.SLOT_NOT_SELECTED) {
            getSlotView(selectedSlotIndex).setSelected(false);
        }

        selectedSlotIndex = Config.SLOT_NOT_SELECTED;

        detailsTable.clearChildren();
        detailsTable.add(new Label("Empty slot.", getSkin()));
    }

    @Override
    public void moveSelectionLeft() {
        if (selectFirstSlotIfNeeded()) return;

        boolean leftEdge = (selectedSlotIndex % Config.HOTBAR_SIZE == 0);
        if (leftEdge){
            selectSlot(selectedSlotIndex + Config.HOTBAR_SIZE - 1);
            return;
        }

        selectSlot(selectedSlotIndex - 1);
    }

    @Override
    public void moveSelectionRight() {
        if (selectFirstSlotIfNeeded()) return;

        boolean rightEdge = (selectedSlotIndex % Config.HOTBAR_SIZE == Config.HOTBAR_SIZE - 1);

        if (rightEdge){
            selectSlot(selectedSlotIndex - Config.HOTBAR_SIZE + 1);
            return;
        }

        selectSlot(selectedSlotIndex + 1);
    }

    @Override
    public void moveSelectionUp() {
        if (selectFirstSlotIfNeeded()) return;

        boolean downEdge = selectedSlotIndex < Config.HOTBAR_SIZE;
        if (downEdge){
            selectSlot(selectedSlotIndex + Config.INVENTORY_SIZE - Config.HOTBAR_SIZE);
            return;
        }

        selectSlot(selectedSlotIndex - Config.HOTBAR_SIZE);
    }

    @Override
    public void moveSelectionDown() {
        if (selectFirstSlotIfNeeded()) return;

        selectSlot((selectedSlotIndex + Config.HOTBAR_SIZE) % Config.INVENTORY_SIZE);
    }

    private boolean selectFirstSlotIfNeeded() {
        if (selectedSlotIndex != Config.SLOT_NOT_SELECTED) {
            return false;
        }

        selectSlot(Config.HOTBAR_SIZE);
        return true;
    }

    @Override
    public void confirmSelection() {
        if (selectedSlotIndex == Config.SLOT_NOT_SELECTED) {
            selectSlot(Config.HOTBAR_SIZE);
            return;
        }

        if (keyboardMoveSourceIndex == Config.SLOT_NOT_SELECTED) {
            if (inventory.getSlot(selectedSlotIndex) == null) return;

            beginKeyboardMove(selectedSlotIndex);
            return;
        }

        if (keyboardMoveSourceIndex == selectedSlotIndex) {
            clearKeyboardMove();
            return;
        }

        boolean moved = inventory.moveSlot(
            keyboardMoveSourceIndex,
            selectedSlotIndex
        );

        if (!moved) return;

        clearKeyboardMove();
        refresh();
    }

    private void beginKeyboardMove(int sourceIndex) {
        keyboardMoveSourceIndex = sourceIndex;
        getSlotView(sourceIndex).setKeyboardPickedUp(true);
    }

    private void clearKeyboardMove() {
        if (keyboardMoveSourceIndex != Config.SLOT_NOT_SELECTED) getSlotView(keyboardMoveSourceIndex).setKeyboardPickedUp(false);

        keyboardMoveSourceIndex = Config.SLOT_NOT_SELECTED;
    }

    private InventorySlotUi getSlotView(int slotIndex){
        if (slotIndex < Config.HOTBAR_SIZE){
            return hotbarGrid.getSlotView(slotIndex);
        }
        return mainGrid.getSlotView(slotIndex);
    }
}
