package com.ashveil.objects;

import com.ashveil.Config;
import com.ashveil.items.inventory.Inventory;

public class Chest extends DestructibleObject{

    private final Inventory chestInventory;

    public Chest(float x, float y) {
        this(x, y, DestructibleObjectType.CHEST.getHp());
    }

    public Chest(float x, float y, int currentHp){
        super(x, y, DestructibleObjectType.CHEST, currentHp);
        chestInventory = new Inventory(Config.CHEST_INVENTORY_SIZE);
    }

    public Inventory getChestInventory() {return chestInventory;}
}
