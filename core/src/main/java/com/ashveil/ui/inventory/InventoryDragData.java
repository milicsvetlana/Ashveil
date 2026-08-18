package com.ashveil.ui.inventory;

import com.ashveil.items.inventory.Inventory;

public class InventoryDragData {
    private final int sourceSlotIndex;
    private final int draggedQuantity;
    private final boolean splitDrag;
    private final Inventory sourceInventory;

    public InventoryDragData(int sourceSlotIndex, int draggedQuantity, boolean splitDrag, Inventory sourceInventory){
        this.sourceSlotIndex = sourceSlotIndex;
        this.draggedQuantity = draggedQuantity;
        this.splitDrag = splitDrag;
        this.sourceInventory = sourceInventory;
    }

    public int getSourceSlotIndex() {return sourceSlotIndex;}
    public int getDraggedQuantity() {return draggedQuantity;}
    public boolean isSplitDrag() {return splitDrag;}
    public Inventory getSourceInventory() {return sourceInventory;}
}
