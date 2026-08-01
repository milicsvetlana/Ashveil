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

    public boolean moveSlot(int sourceIndex, int destinationIndex){
        if (sourceIndex < 0 || sourceIndex >= slots.length) return false;
        if (destinationIndex < 0 || destinationIndex >= slots.length) return false;
        if (sourceIndex == destinationIndex) return false;

        ItemStack itemStack1 = getSlot(sourceIndex);
        if (itemStack1 == null) return false;
        ItemStack itemStack2 = getSlot(destinationIndex);

        if (itemStack2 == null){
            slots[destinationIndex] = itemStack1;
            removeFromSlot(sourceIndex, itemStack1.getQuantity());
            return true;
        }

        if (itemStack1.getType() != itemStack2.getType()
            || (itemStack1.getType() == itemStack2.getType() && !itemStack1.getType().isStackable())){
            slots[destinationIndex] = itemStack1;
            slots[sourceIndex] = itemStack2;
            return true;
        }

        int leftOver = itemStack2.addQuantity(itemStack1.getQuantity());
        int moved = itemStack1.getQuantity() - leftOver;

        if (moved == 0) return false;

        itemStack1.reduceQuantity(moved);

        if (itemStack1.isEmpty()){
            slots[sourceIndex] = null;
        }

        return true;
    }

    public boolean splitStack(int sourceIndex, int destinationIndex, int amount){
        if (sourceIndex < 0 || sourceIndex >= slots.length) return false;
        if (destinationIndex < 0 || destinationIndex >= slots.length) return false;
        if (sourceIndex == destinationIndex) return false;

        ItemStack itemStack1 = getSlot(sourceIndex);
        ItemStack itemStack2 = getSlot(destinationIndex);
        if (itemStack1 == null || itemStack1.getQuantity() <= amount || 0 >= amount
            || itemStack2 != null || !itemStack1.getType().isStackable()) return false;

        slots[destinationIndex] = new ItemStack(itemStack1.getType(), amount);
        itemStack1.reduceQuantity(amount);

        return true;
    }

}



















