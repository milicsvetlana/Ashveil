package com.ashveil.items.inventory;

import com.ashveil.Config;

public class Inventory {
    ItemStack[] slots;

    public Inventory() {
        this.slots = new ItemStack[Config.INVENTORY_SIZE];
    }

    //true znači da je predmet dodat ili spojen s postojećim stackom
    //false znači da je inventori pun
    public boolean addItem(ItemType type, int amount){
        for (ItemStack item : slots){
            if(item == null) continue;
            if (type.equals(item.getType())){
                int current = item.getQuantity();
                item.setQuantity(current + amount);
                return true;
            }
        }
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = new ItemStack(type, amount);
                return true;
            }
        }
        return false;
    }

    public void removeItem(ItemType type, int amount){
        for (int i=0; i< slots.length; i++){
            ItemStack item = slots[i];
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
