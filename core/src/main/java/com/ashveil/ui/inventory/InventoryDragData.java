package com.ashveil.ui.inventory;

public class InventoryDragData {
    private final int sourceSlotIndex;

    public InventoryDragData(int sourceSlotIndex){
        this.sourceSlotIndex = sourceSlotIndex;
    }

    public int getSourceSlotIndex() {return sourceSlotIndex;}

}
