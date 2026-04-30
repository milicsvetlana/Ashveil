package com.ashveil.items.inventory;

import com.ashveil.Config;

public class Inventory {
    ItemStack[] slots;

    public Inventory() {
        this.slots = new ItemStack[Config.INVENTORY_SIZE];
    }

    public void addItem(ItemType type, int amount){
        boolean found = false;
        for (ItemStack item : slots){
            if(item == null) continue;
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

    public void removeItem(ItemType type, int amount){
        int i = 0;
        for (ItemStack item : slots){
            if(item == null) continue;
            if (type.equals(item.getType())){
                int current = item.getQuantity();
                if (current <= amount){
                    amount -= current;
                    slots[i] = null;
                }
                else{
                    item.setQuantity(current - amount);
                    return;
                }
            }
            i++;
        }
    }

    public int getQuantity(ItemType type){
        boolean found = false;
        for (ItemStack item : slots){
            if(item == null) continue;
            if (type.equals(item.getType())){
                return item.getQuantity();
            }
        }
        return 0;
    }

    public ItemStack[] getSlots() {
        return slots;
    }

}
