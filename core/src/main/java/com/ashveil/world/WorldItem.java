package com.ashveil.world;

import com.ashveil.Config;
import com.ashveil.items.inventory.ItemType;

public class WorldItem {
    private final float x, y;
    private final ItemType type;
    private int amount;
    private float lifetime;

    public WorldItem(float x, float y, ItemType type, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        this.x = x;
        this.y = y;
        this.type = type;
        this.amount = amount;
        this.lifetime = 0;
    }

    public void update(float delta){
        lifetime += delta;
    }

    public int addAmount(int newAmount){ //returns how much we weren't able to stack up
        if (newAmount <= 0) throw new IllegalArgumentException("Amount must be positive.");

        if (type.getMaxStack() < this.amount + newAmount){
            int current = this.amount;
            this.amount = type.getMaxStack();
            return current - type.getMaxStack() + newAmount;
        }
        this.amount += newAmount;
        return 0;
    }

    public void setAmount(int newAmount){
        if (newAmount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        this.amount = newAmount;
    }

    public boolean shouldDespawn(){
        return type.despawnsOnGround() && lifetime >= Config.WORLD_ITEM_DESPAWN_TIME;
    }

    public void resetLifetime(){lifetime = 0f;}

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
