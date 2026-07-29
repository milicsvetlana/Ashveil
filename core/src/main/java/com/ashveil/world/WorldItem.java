package com.ashveil.world;

import com.ashveil.items.inventory.ItemType;

public class WorldItem {
    private final float x, y;
    private final ItemType type;
    private int amount;

    public WorldItem(float x, float y, ItemType type, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        this.x = x;
        this.y = y;
        this.type = type;
        this.amount = amount;
    }

    public void setAmount(int amount){
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        this.amount = amount;
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public ItemType getType() {
        return type;
    }
    public int getAmount() {
        return amount;
    }

}
