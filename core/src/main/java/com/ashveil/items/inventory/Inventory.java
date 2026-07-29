package com.ashveil.items.inventory;

import com.ashveil.Config;

public class Inventory {
    private final ItemStack[] slots;

    public Inventory() {
        this.slots = new ItemStack[Config.INVENTORY_SIZE];
    }

    public int addItem(ItemType type, int amount){
        if (type == null) throw new IllegalArgumentException("Item type cannot be null");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");

        for (ItemStack item : slots){
            if (item == null) continue;
            if (type.equals(item.getType())){
                int notAdded = item.addQuantity(amount);
                if (notAdded == 0) return 0;
                amount = notAdded;
            }
        }
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                int notAdded = 0;
                if (amount > type.getMaxStack()){
                    notAdded = amount - type.getMaxStack();
                    amount = type.getMaxStack();
                }
                slots[i] = new ItemStack(type, amount);
                if (notAdded <= 0) return 0;
                amount = notAdded;
            }
        }
        return amount;
    }

    public boolean removeItem(ItemType type, int amount){
        if (type == null) throw new IllegalArgumentException("Item type cannot be null");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");

        if (getQuantity(type) < amount) return false;
        int remaining = amount;

        for (int i=0; i < slots.length; i++){
            ItemStack item = slots[i];
            if(item == null) continue;
            if (!type.equals(item.getType())) continue;

            int removed = item.reduceQuantity(remaining);
            remaining -= removed;

            if (item.isEmpty()) slots[i] = null;

            if (remaining == 0) return true;
        }
        return remaining == 0;
    }

    public int getQuantity(ItemType type){
        int total = 0;
        for (ItemStack item : slots){
            if(item == null) continue;
            if (type.equals(item.getType())){
                total += item.getQuantity();
            }
        }
        return total;
    }

    public ItemStack getSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.length) return null;
        return slots[slotIndex];
    }

    public ItemType getItemTypeBySlot(int slotIndex){
        if (slotIndex < 0 || slotIndex >= slots.length) return null;
        if (slots[slotIndex] == null) return null;
        return slots[slotIndex].getType();
    }
    public int getQuantityBySlot(int slotIndex){
        if (slotIndex < 0 || slotIndex >= slots.length) return 0;
        if (slots[slotIndex] == null) return 0;
        return slots[slotIndex].getQuantity();
    }
    public int removeFromSlot(int slotIndex, int amount){ //vraca uklonjenu kolicinu
        if (slotIndex < 0 || slotIndex >= slots.length) return 0;
        if (amount <= 0) return 0;
        ItemStack item = slots[slotIndex];
        if(item == null) return 0;

        int current = item.getQuantity();
        if (current <= amount){
            slots[slotIndex] = null;
            return current;
        }
        else{
            item.reduceQuantity(amount);
            return amount;
        }
    }
}
