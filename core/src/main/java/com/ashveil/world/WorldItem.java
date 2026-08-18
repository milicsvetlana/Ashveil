package com.ashveil.world;

import com.ashveil.Config;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.inventory.ItemType;

public class WorldItem {
    private final float x, y;
    private ItemStack stack;
    private float lifetime;

    public WorldItem(float x, float y, ItemType type, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        this.x = x;
        this.y = y;
        stack = new ItemStack(type, amount);
        this.lifetime = 0;
    }

    public WorldItem(float x, float y, ItemStack itemStack){
        if (itemStack == null) throw new IllegalArgumentException("ItemStack can't be null.");
        if (itemStack.getQuantity() <= 0) throw new IllegalArgumentException("Amount must be positive.");
        this.x = x;
        this.y = y;
        stack = itemStack;
        this.lifetime = 0;
    }

    public void update(float delta){
        lifetime += delta;
    }

    public int addAmount(int newAmount){
        if (newAmount <= 0) throw new IllegalArgumentException("Amount must be positive.");

        int currentAmount = stack.getQuantity();
        int maxStack = stack.getType().getMaxStack();
        int availableSpace = maxStack - currentAmount;

        if (newAmount > availableSpace){
            stack = new ItemStack(stack.getType(), maxStack);
            return newAmount - availableSpace;
        }

        stack = new ItemStack(stack.getType(), currentAmount + newAmount);
        return 0;
    }

    public void setAmount(int newAmount){
        if (newAmount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        stack = new ItemStack(stack.getType(), newAmount);
    }

    public boolean shouldDespawn(){
        return stack.getType().despawnsOnGround() && lifetime >= Config.WORLD_ITEM_DESPAWN_TIME;
    }

    public void resetLifetime(){lifetime = 0f;}

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public ItemType getType() {
        return stack.getType();
    }
    public int getAmount() {
        return stack.getQuantity();
    }
    public ItemStack getStack(){return stack;}
}
