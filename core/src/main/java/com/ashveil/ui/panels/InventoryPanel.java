package com.ashveil.ui.panels;

import com.ashveil.Config;
import com.ashveil.items.inventory.Inventory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.ui.inventory.InventorySlotUi;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.ArrayList;
import java.util.List;

//prikazuje svih 20 slotova
public class InventoryPanel extends MenuPanel {

    private final Inventory inventory;
    private int selectedSlotIndex = Config.SLOT_NOT_SELECTED;

    private final Table mainSlotsTable;
    private final Table hotbarTable;
    private final Table detailsTable;
    private final List<InventorySlotUi> slotViews;

    public InventoryPanel(Skin skin, Inventory inventory) {
        super(skin);
        this.inventory = inventory;

        slotViews = new ArrayList<>();
        mainSlotsTable = new Table();
        hotbarTable = new Table();
        detailsTable = new Table();

        createSlots(skin);
        createLayout(skin);
        refresh();
    }

    @Override
    public void refresh(){
        for (int i=0; i < Config.INVENTORY_SIZE; i++){
            slotViews.get(i).refresh(inventory.getSlot(i));
        }
        if (selectedSlotIndex != Config.SLOT_NOT_SELECTED) {
            updateDetails();
        }
    }

    private void createSlots(Skin skin){
        for (int i=0; i < Config.INVENTORY_SIZE; i++){
            int slotIndex = i;

            InventorySlotUi slotUi = new InventorySlotUi(i, i < Config.HOTBAR_SIZE, skin);
            slotUi.addListener(new InputListener(){
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    selectSlot(slotIndex);
                }
            });
            slotViews.add(slotUi);
        }
    }

    private void createLayout(Skin skin){
        setBackground(getSkin().getDrawable("inventory-panel-background"));
        pad(20);

        int localCounter = 0;
        for (int i=Config.HOTBAR_SIZE; i < Config.INVENTORY_SIZE; i++){
            localCounter++;
            mainSlotsTable.add(slotViews.get(i)).size(64).pad(5);
            if (localCounter == 5) {
                localCounter = 0;
                mainSlotsTable.row();
            }
        }

        for (int i=0; i < Config.HOTBAR_SIZE; i++){
            hotbarTable.add(slotViews.get(i)).size(64).pad(5);
        }

        Table inventorySection = new Table();

        inventorySection.add(new Label("Inventory", skin));
        inventorySection.row();
        inventorySection.add(mainSlotsTable);
        inventorySection.row();
        inventorySection.add(new Label("Hotbar", skin));
        inventorySection.row();
        inventorySection.add(hotbarTable);

        detailsTable.top().left();
        detailsTable.add(new Label("Empty slot.", skin));
        add(inventorySection).grow();
        add(detailsTable).width(260).growY().padLeft(30);
    }

    private void selectSlot(int slotIndex){
        if (selectedSlotIndex == slotIndex) return;
        if (slotIndex < 0 || slotIndex >= Config.INVENTORY_SIZE) return;
        if (selectedSlotIndex != Config.SLOT_NOT_SELECTED) slotViews.get(selectedSlotIndex).setSelected(false);
        selectedSlotIndex = slotIndex;
        slotViews.get(selectedSlotIndex).setSelected(true);
        updateDetails();
    }

    private void updateDetails(){
        detailsTable.clearChildren();
        ItemStack item = inventory.getSlot(selectedSlotIndex);
        if (item == null) {
            detailsTable.add(new Label("No item selected.", getSkin()));
            return;
        }
        detailsTable.add(new Label("Name: " + item.getType().getDisplayName(), getSkin())).left();
        detailsTable.row();
        detailsTable.add(new Label("Description: " + item.getType().getDescription(), getSkin())).padTop(20).left();
        detailsTable.row();
        if (item.getType().usesDurability()) detailsTable.add(new Label("Durability: " + item.getDurability() + " / " + item.getType().getMaxDurability(), getSkin())).left();
        detailsTable.row();
        if (item.getType().isStackable()) detailsTable.add(new Label("Quantity: " + item.getQuantity(), getSkin())).left();
    }

    @Override
    public void onHide(){
        clearSelection();
    }

    private void clearSelection() {
        if (selectedSlotIndex != Config.SLOT_NOT_SELECTED) {
            slotViews.get(selectedSlotIndex).setSelected(false);
        }

        selectedSlotIndex = Config.SLOT_NOT_SELECTED;

        detailsTable.clearChildren();
        detailsTable.add(new Label("Empty slot.", getSkin()));
    }

}
