package com.ashveil.items.inventory;

import com.ashveil.Config;

public class Inventory {
    private final ItemStack[] slots;

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
        }
    }

    public int getQuantity(ItemType type){
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
    public void removeFromSlot(int slotIndex, int amount){
        if (slotIndex < 0 || slotIndex >= slots.length) return;
        if (amount <= 0) return;
        ItemStack item = slots[slotIndex];
        if(item == null) return;

        int current = item.getQuantity();
        if (current <= amount){
            slots[slotIndex] = null;
        }
        else{
            item.setQuantity(current - amount);
        }
    }
}
