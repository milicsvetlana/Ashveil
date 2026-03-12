package com.ashveil.items;

public class ItemStack {
    ItemType type;
    int quantity;

    public ItemStack(ItemType type, int quantity) {
        this.type = type;
        this.quantity = quantity;
    }

    public ItemType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
