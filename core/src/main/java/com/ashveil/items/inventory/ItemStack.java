package com.ashveil.items.inventory;

public class ItemStack {
    private final ItemType type;
    private int quantity;
    private int durability;

    public ItemStack(ItemType type, int quantity) {
        if (type == null) throw new IllegalArgumentException("Item type cannot be null");
        if (quantity <= 0 || quantity > type.getMaxStack()) throw new IllegalArgumentException("Invalid item stack quantity");
        this.type = type;
        this.quantity = quantity;
        this.durability = type.getMaxDurability();
    }

    public ItemType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    int addQuantity(int amount){ //vraca koliko nije dodato
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        int max = type.getMaxStack();
        if (quantity + amount > max){
            int diff = amount - (max - quantity);
            quantity = max;
            return  diff;
        }
        quantity += amount;
        return 0;
    }

    int removeQuantity(int amount){ //vraca koliko je uklonjeno
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (quantity < amount){
            int current = quantity;
            quantity = 0;
            return current;
        }
        quantity -= amount;
        return amount;
    }

    public boolean reduceDurability(int amount){
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (!type.usesDurability()) return false;
        durability -= amount;
        if (durability <= 0) {
            durability = 0;
            return true;
        }
        return false;
    }

    public int getDurability() {return durability;}

    public boolean isEmpty(){
        return quantity == 0;
    }

}
