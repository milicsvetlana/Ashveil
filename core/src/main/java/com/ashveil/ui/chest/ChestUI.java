package com.ashveil.ui.chest;

import com.ashveil.Config;
import com.ashveil.items.inventory.Inventory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.ui.inventory.InventoryGridUi;
import com.ashveil.ui.inventory.InventorySlotUi;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

public class ChestUI extends Table {
    private final Inventory playerInventory;
    private final Inventory chestInventory;

    private final InventoryGridUi playerMainGrid;
    private final InventoryGridUi playerHotbarGrid;
    private final InventoryGridUi chestGrid;

    private final DragAndDrop dragAndDrop;

    private Inventory selectedInventory;
    private int selectedSlotIndex;
    private Inventory keyboardMoveSourceInventory;
    private int keyboardMoveSourceIndex;

    public ChestUI(Skin skin, Inventory playerInventory, Inventory chestInventory){
        this.playerInventory = playerInventory;
        this.chestInventory = chestInventory;
        selectedInventory = null;
        selectedSlotIndex = Config.SLOT_NOT_SELECTED;
        keyboardMoveSourceInventory = null;
        keyboardMoveSourceIndex = Config.SLOT_NOT_SELECTED;

        dragAndDrop = new DragAndDrop();
        dragAndDrop.setDragTime(0);
        dragAndDrop.setDragActorPosition(28f, -28f);

        playerMainGrid = new InventoryGridUi(skin, playerInventory, Config.HOTBAR_SIZE, playerInventory.getSize() - Config.HOTBAR_SIZE, Config.HOTBAR_SIZE, false, dragAndDrop);
        playerHotbarGrid = new InventoryGridUi(skin, playerInventory, 0, Config.HOTBAR_SIZE, Config.HOTBAR_SIZE, true, dragAndDrop);
        chestGrid = new InventoryGridUi(skin, chestInventory, 0, chestInventory.getSize(), Config.HOTBAR_SIZE, false, dragAndDrop);

        createLayout(skin);
        refresh();
    }

    private void createLayout(Skin skin) {
        setBackground(skin.getDrawable("inventory-panel-background"));
        pad(25);

        Table playerSection = new Table();

        playerSection.add(new Label("Inventory", skin));
        playerSection.row();
        playerSection.add(playerMainGrid);

        playerSection.row();
        playerSection.add(new Label("Hotbar", skin)).padTop(10);
        playerSection.row();
        playerSection.add(playerHotbarGrid);

        Table chestSection = new Table();

        chestSection.add(new Label("Chest", skin));
        chestSection.row();
        chestSection.add(chestGrid);

        add(playerSection).top().padRight(35);
        add(chestSection).top();
    }

    public void refresh() {
        playerMainGrid.refresh();
        playerHotbarGrid.refresh();
        chestGrid.refresh();
    }

    private InventorySlotUi getSlotView(Inventory inventory, int slotIndex){
        if (inventory == playerInventory){
            if (slotIndex < Config.HOTBAR_SIZE) return playerHotbarGrid.getSlotView(slotIndex);
            return playerMainGrid.getSlotView(slotIndex);
        }
        if (inventory == chestInventory) return chestGrid.getSlotView(slotIndex);
        return null;
    }

    private void selectSlot(Inventory inventory, int slotIndex){
        if (inventory == null) return;
        if (slotIndex < 0 || slotIndex >= inventory.getSize()) return;

        if (selectedInventory == inventory && selectedSlotIndex == slotIndex) return;

        InventorySlotUi previousSlot = getSlotView(selectedInventory, selectedSlotIndex);

        if (previousSlot != null) previousSlot.setSelected(false);

        selectedInventory = inventory;
        selectedSlotIndex = slotIndex;

        InventorySlotUi newSlot = getSlotView(selectedInventory, selectedSlotIndex);
        if (newSlot != null) newSlot.setSelected(true);
    }

    private boolean selectFirstSlotIfNeeded(){
        if (selectedInventory != null) return false;

        selectSlot(playerInventory, Config.HOTBAR_SIZE);
        return true;
    }

    public void moveSelectionRight(){
        if (selectFirstSlotIfNeeded()) return;

        if (selectedInventory == playerInventory){
            if (selectedSlotIndex < Config.HOTBAR_SIZE){
                selectSlot(playerInventory, (selectedSlotIndex + 1) % Config.HOTBAR_SIZE);
                return;
            }

            boolean rightEdge = selectedSlotIndex % Config.HOTBAR_SIZE == Config.HOTBAR_SIZE - 1;
            if (rightEdge){
                int row = (selectedSlotIndex - Config.HOTBAR_SIZE) / Config.HOTBAR_SIZE;
                selectSlot(chestInventory, row * Config.HOTBAR_SIZE);
                return;
            }
            selectSlot(playerInventory, selectedSlotIndex + 1);
            return;
        }

        if (selectedInventory == chestInventory){
            boolean rightEdge = selectedSlotIndex % Config.HOTBAR_SIZE == Config.HOTBAR_SIZE - 1;
            if (rightEdge){selectSlot(chestInventory, selectedSlotIndex - Config.HOTBAR_SIZE + 1);
                return;
            }
            selectSlot(chestInventory, selectedSlotIndex + 1);
        }
    }

    public void moveSelectionLeft(){
        if (selectFirstSlotIfNeeded()) return;

        if (selectedInventory == playerInventory){
            if (selectedSlotIndex < Config.HOTBAR_SIZE){
                if (selectedSlotIndex == 0)selectSlot(playerInventory, Config.HOTBAR_SIZE - 1);
                else selectSlot(playerInventory, selectedSlotIndex - 1);
                return;
            }

            boolean leftEdge = selectedSlotIndex % Config.HOTBAR_SIZE == 0;
            if (leftEdge){
                selectSlot(playerInventory, selectedSlotIndex + Config.HOTBAR_SIZE - 1);
                return;
            }
            selectSlot(playerInventory, selectedSlotIndex - 1);
            return;
        }

        if (selectedInventory == chestInventory){
            boolean leftEdge = selectedSlotIndex % Config.HOTBAR_SIZE == 0;
            if (leftEdge){
                int row = selectedSlotIndex / Config.HOTBAR_SIZE;
                int playerSlot = Config.HOTBAR_SIZE + row * Config.HOTBAR_SIZE + Config.HOTBAR_SIZE - 1;
                selectSlot(playerInventory, playerSlot);
                return;
            }
            selectSlot(chestInventory, selectedSlotIndex - 1);
        }
    }

    public void moveSelectionUp(){
        if (selectFirstSlotIfNeeded()) return;
        if (selectedInventory == playerInventory){
            if (selectedSlotIndex < Config.HOTBAR_SIZE){
                selectSlot(playerInventory, selectedSlotIndex + playerInventory.getSize() - Config.HOTBAR_SIZE);
                return;
            }

            if (selectedSlotIndex < Config.HOTBAR_SIZE * 2){
                selectSlot(playerInventory, selectedSlotIndex - Config.HOTBAR_SIZE);
                return;
            }
            selectSlot(playerInventory, selectedSlotIndex - Config.HOTBAR_SIZE);
            return;
        }

        if (selectedInventory == chestInventory){
            if (selectedSlotIndex < Config.HOTBAR_SIZE){
                selectSlot(chestInventory, selectedSlotIndex + chestInventory.getSize() - Config.HOTBAR_SIZE);
                return;
            }
            selectSlot(chestInventory, selectedSlotIndex - Config.HOTBAR_SIZE);
        }
    }

    public void moveSelectionDown(){
        if (selectFirstSlotIfNeeded()) return;

        if (selectedInventory == playerInventory){
            if (selectedSlotIndex < Config.HOTBAR_SIZE){
                selectSlot(playerInventory, selectedSlotIndex + Config.HOTBAR_SIZE);
                return;
            }

            if (selectedSlotIndex >= playerInventory.getSize() - Config.HOTBAR_SIZE){
                selectSlot(playerInventory, selectedSlotIndex - playerInventory.getSize() + Config.HOTBAR_SIZE);
                return;
            }
            selectSlot(playerInventory, selectedSlotIndex + Config.HOTBAR_SIZE);
            return;
        }

        if (selectedInventory == chestInventory){

            if (selectedSlotIndex >= chestInventory.getSize() - Config.HOTBAR_SIZE){
                selectSlot(chestInventory, selectedSlotIndex - chestInventory.getSize() + Config.HOTBAR_SIZE);
                return;
            }
            selectSlot(chestInventory, selectedSlotIndex + Config.HOTBAR_SIZE);
        }
    }

    private void beginKeyboardMove(){
        if (selectedInventory == null) return;
        ItemStack selectedStack = selectedInventory.getSlot(selectedSlotIndex);
        if (selectedStack == null) return;
        keyboardMoveSourceInventory = selectedInventory;
        keyboardMoveSourceIndex = selectedSlotIndex;
        getSlotView(keyboardMoveSourceInventory, keyboardMoveSourceIndex).setKeyboardPickedUp(true);
    }

    private void clearKeyboardMove(){
        if (keyboardMoveSourceInventory != null && keyboardMoveSourceIndex != Config.SLOT_NOT_SELECTED){
            InventorySlotUi sourceSlot = getSlotView(keyboardMoveSourceInventory, keyboardMoveSourceIndex);
            if (sourceSlot != null) sourceSlot.setKeyboardPickedUp(false);
        }
        keyboardMoveSourceInventory = null;
        keyboardMoveSourceIndex = Config.SLOT_NOT_SELECTED;
    }

    public void confirmSelection(){
        if (selectedInventory == null) {
            selectSlot(playerInventory, Config.HOTBAR_SIZE);
            return;
        }

        if (keyboardMoveSourceInventory == null) {
            beginKeyboardMove();
            return;
        }

        if (keyboardMoveSourceInventory == selectedInventory && keyboardMoveSourceIndex == selectedSlotIndex){
            clearKeyboardMove();
            return;
        }

        boolean moved = keyboardMoveSourceInventory.moveSlot(keyboardMoveSourceIndex, selectedInventory, selectedSlotIndex);
        if (!moved) return;

        clearKeyboardMove();
        refresh();
    }
}
