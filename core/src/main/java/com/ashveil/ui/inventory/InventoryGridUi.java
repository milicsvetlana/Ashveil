package com.ashveil.ui.inventory;

import com.ashveil.items.inventory.Inventory;
import com.ashveil.items.inventory.ItemStack;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

import java.util.ArrayList;
import java.util.List;

public class InventoryGridUi extends Table {
    private final Inventory inventory;
    private final List<InventorySlotUi> slotViews;
    private final int columns;
    private final int startIndex;
    private final int slotCount;
    private final boolean hotbarStyle;
    private final DragAndDrop dragAndDrop;

    public InventoryGridUi(Skin skin, Inventory inventory, int startIndex, int slotCount, int columns, boolean hotbarStyle, DragAndDrop dragAndDrop){
        this.inventory = inventory;
        this.columns = columns;
        this.slotViews = new ArrayList<>();
        this.dragAndDrop = dragAndDrop;
        this.startIndex = startIndex;
        this.slotCount = slotCount;
        this.hotbarStyle = hotbarStyle;

        createSlots(skin);
        createLayout();
        refresh();
    }

    private void createSlots(Skin skin){
        int endIndex = startIndex + slotCount;
        for (int i=startIndex; i < endIndex; i++){
            InventorySlotUi slotUi = new InventorySlotUi(i, hotbarStyle, skin);
            slotViews.add(slotUi);
            registerDragAndDrop(slotUi, skin);
        }
    }

    private void createLayout(){
        int column = 0;

        for (InventorySlotUi slotUi : slotViews){
            add(slotUi).size(64).pad(5);
            column++;
            if (column == columns){
                row();
                column = 0;
            }
        }
    }

    private void registerDragAndDrop(InventorySlotUi slotUi, Skin skin){
        dragAndDrop.addSource(new DragAndDrop.Source(slotUi) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent inputEvent, float x, float y, int pointer) {
                int sourceIndex = slotUi.getSlotIndex();
                ItemStack sourceStack = inventory.getSlot(sourceIndex);

                if (sourceStack == null) return null;

                boolean shiftPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
                boolean splitDrag = shiftPressed && sourceStack.getType().isStackable() && sourceStack.getQuantity() > 1;
                int draggedQuantity = splitDrag ? (sourceStack.getQuantity() + 1) / 2 : sourceStack.getQuantity();

                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                payload.setObject(new InventoryDragData(sourceIndex, draggedQuantity, splitDrag, inventory));

                Image dragImage = new Image(skin.getDrawable("item-placeholder"));
                dragImage.setColor(InventorySlotUi.getTemporaryColor(sourceStack.getType()));
                Label dragQuantity = new Label("", skin);
                if (draggedQuantity > 1) dragQuantity.setText(String.valueOf(draggedQuantity));

                Stack dragActor = new Stack();
                Table imageLayer = new Table();
                imageLayer.add(dragImage).size(48);

                Table quantityLayer = new Table();
                quantityLayer.bottom().right();
                quantityLayer.add(dragQuantity).pad(3);

                dragActor.add(imageLayer);
                dragActor.add(quantityLayer);

                dragActor.setSize(56, 56);
                dragActor.setTouchable(Touchable.disabled);

                payload.setDragActor(dragActor);

                return payload;
            }
        });
        dragAndDrop.addTarget(new DragAndDrop.Target(slotUi) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                InventoryDragData dragData = (InventoryDragData) payload.getObject();

                int sourceIndex = dragData.getSourceSlotIndex();
                int destinationIndex = slotUi.getSlotIndex();

                if (dragData.getSourceInventory() == inventory
                    && sourceIndex == destinationIndex) {
                    return false;
                }

                if (dragData.isSplitDrag()) {
                    return inventory.getSlot(destinationIndex) == null;
                }

                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload,float x, float y, int pointer) {
                InventoryDragData dragData = (InventoryDragData) payload.getObject();
                Inventory sourceInventory = dragData.getSourceInventory();

                int sourceIndex = dragData.getSourceSlotIndex();
                int destinationIndex = slotUi.getSlotIndex();
                boolean moved;

                if (dragData.isSplitDrag()) moved = sourceInventory.splitStack(sourceIndex, inventory, destinationIndex, dragData.getDraggedQuantity());
                else moved = sourceInventory.moveSlot(sourceIndex, inventory, destinationIndex);

                if (!moved) return;

                InventorySlotUi sourceSlotUi = (InventorySlotUi) source.getActor();
                sourceSlotUi.refresh(sourceInventory.getSlot(sourceIndex));
                slotUi.refresh(inventory.getSlot(destinationIndex));
            }
        });
    }

    public void refresh(){
        for (InventorySlotUi slotUi : slotViews){
            int slotIndex = slotUi.getSlotIndex();
            slotUi.refresh(inventory.getSlot(slotIndex));
        }
    }

    public InventorySlotUi getSlotView(int inventorySlotIndex){
        for (InventorySlotUi slotUi : slotViews){
            if (slotUi.getSlotIndex() == inventorySlotIndex) return slotUi;
        }
        return null;
    }
}
