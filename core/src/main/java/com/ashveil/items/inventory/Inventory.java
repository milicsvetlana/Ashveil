package com.ashveil.items.inventory;

import com.ashveil.Config;

public class Inventory {
    private final ItemStack[] slots;

    public Inventory() {this(Config.INVENTORY_SIZE);}
    public Inventory(int size) {this.slots = new ItemStack[size];}

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
        return moveSlot(sourceIndex, this, destinationIndex);
    }

    public boolean moveSlot(int sourceIndex, Inventory destinationInventory, int destinationIndex){
        if (destinationInventory == null) return false;

        if (sourceIndex < 0 || sourceIndex >= slots.length) return false;
        if (destinationIndex < 0 || destinationIndex >= destinationInventory.slots.length) return false;

        if (this == destinationInventory && sourceIndex == destinationIndex) return false;

        ItemStack sourceStack = getSlot(sourceIndex);
        if (sourceStack == null) return false;

        ItemStack destinationStack = destinationInventory.getSlot(destinationIndex);

        if (destinationStack == null){
            destinationInventory.slots[destinationIndex] = sourceStack;
            slots[sourceIndex] = null;
            return true;
        }

        if (sourceStack.getType() != destinationStack.getType()
            || !sourceStack.getType().isStackable()) {

            destinationInventory.slots[destinationIndex] = sourceStack;
            slots[sourceIndex] = destinationStack;
            return true;
        }

        int leftOver = destinationStack.addQuantity(sourceStack.getQuantity());
        int moved = sourceStack.getQuantity() - leftOver;

        if (moved == 0) return false;

        sourceStack.reduceQuantity(moved);

        if (sourceStack.isEmpty()){
            slots[sourceIndex] = null;
        }

        return true;
    }

    public boolean splitStack(int sourceIndex, int destinationIndex, int amount){
        return splitStack(sourceIndex, this, destinationIndex, amount);
    }

    public boolean splitStack(int sourceIndex, Inventory destinationInventory, int destinationIndex, int amount){
        if (destinationInventory == null) return false;

        if (sourceIndex < 0 || sourceIndex >= slots.length) return false;
        if (destinationIndex < 0 || destinationIndex >= destinationInventory.slots.length) return false;

        if (this == destinationInventory && sourceIndex == destinationIndex) return false;

        ItemStack sourceStack = getSlot(sourceIndex);
        ItemStack destinationStack = destinationInventory.getSlot(destinationIndex);

        if (sourceStack == null || sourceStack.getQuantity() <= amount || amount <= 0 || destinationStack != null
                         || !sourceStack.getType().isStackable()) return false;

        destinationInventory.slots[destinationIndex] =
            new ItemStack(sourceStack.getType(), amount);

        sourceStack.reduceQuantity(amount);

        return true;
    }

    public ItemStack extractFromSlot(int slotIndex, int amount){
        if (slotIndex < 0 || slotIndex >= slots.length) return null;
        if (amount <= 0) return null;

        ItemStack sourceStack = slots[slotIndex];
        if (sourceStack == null) return null;

        if (amount >= sourceStack.getQuantity()){
            slots[slotIndex] = null;
            return sourceStack;
        }

        ItemStack extractedStack = new ItemStack(slots[slotIndex].getType(), amount);
        slots[slotIndex].reduceQuantity(amount);
        return extractedStack;
    }

    public int addStack(ItemStack itemStack){
        if (itemStack == null) throw new IllegalArgumentException("Item stack cannot be null");
        if (itemStack.isEmpty()) return 0;

        if (!itemStack.getType().isStackable()){
            for (int i = 0; i < slots.length; i++){
                if (slots[i] == null){
                    slots[i] = itemStack;
                    return 0;
                }
            }
            return itemStack.getQuantity();
        }

        for (ItemStack destinationStack : slots){
            if (destinationStack == null) continue;
            if (destinationStack.getType() != itemStack.getType()) continue;

            int sourceQuantity = itemStack.getQuantity();
            int leftOver = destinationStack.addQuantity(sourceQuantity);
            int moved = sourceQuantity - leftOver;

            if (moved > 0){
                itemStack.reduceQuantity(moved);
            }

            if (itemStack.isEmpty()) return 0;
        }

        for (int i = 0; i < slots.length; i++){
            if (slots[i] == null){
                slots[i] = itemStack;
                return 0;
            }
        }

        return itemStack.getQuantity();
    }

    public int getSize(){return slots.length;}
}



















