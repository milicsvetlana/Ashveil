package com.ashveil.ui.inventory;

public class InventoryDragData {
    private final int sourceSlotIndex;
    private final int draggedQuantity;
    private final boolean splitDrag;

    public InventoryDragData(int sourceSlotIndex, int draggedQuantity, boolean splitDrag){
        this.sourceSlotIndex = sourceSlotIndex;
        this.draggedQuantity = draggedQuantity;
        this.splitDrag = splitDrag;
    }

    public int getSourceSlotIndex() {return sourceSlotIndex;}
    public int getDraggedQuantity() {return draggedQuantity;}
    public boolean isSplitDrag() {return splitDrag;}
}
