package com.ashveil.items;

import com.ashveil.Config;

public class Inventory {
    ItemStack[] slots;

    public Inventory() {
        this.slots = new ItemStack[Config.INVENTORY_SIZE];
    }

    public void addItem(ItemType type, int amount){
        boolean found = false;
        for (ItemStack item : slots){
            if(item == null) break;
            if (type.equals(item.getType())){
                int current = item.getQuantity();
                item.setQuantity(current += amount);
                found = true;
            }
        }
        if (!found){
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] == null) {
                    slots[i] = new ItemStack(type, amount);
                    return;
                }
            }
        }
    }

    public ItemStack[] getSlots() {
        return slots;
    }
}
