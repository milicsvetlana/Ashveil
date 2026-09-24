package com.ashveil.objects;

import com.ashveil.Config;
import com.ashveil.items.inventory.Inventory;

public class Chest extends DestructibleObject{

    private static final float COLLISION_SIZE = Config.CHEST_WORLD_SIZE;
    private static final float COLLISION_OFFSET_X = (Config.TILE_SIZE - COLLISION_SIZE) / 2f;
    private static final float COLLISION_OFFSET_Y = 0f;

    private final Inventory chestInventory;
    private final ChestKind chestKind;

    public Chest(float x, float y) {
        this(x, y, DestructibleObjectType.CHEST.getHp(), ChestKind.STANDARD);
    }

    public Chest(float x, float y, int currentHp){
        this(x, y, currentHp, ChestKind.STANDARD);
    }

    //pravi chest s defaultnim full  hp, sluzi kada se ingame napravi i postavi chest
    public Chest(float x, float y, ChestKind kind){
        this(x, y, DestructibleObjectType.CHEST.getHp(), kind);
    }

    //koristi se kada se restoruje postojece stanje, npr. iz savea
    public Chest(float x, float y, int currentHp, ChestKind chestKind){
        super(x, y, DestructibleObjectType.CHEST, currentHp, COLLISION_OFFSET_X, COLLISION_OFFSET_Y, COLLISION_SIZE, COLLISION_SIZE);

        if (chestKind == null) throw new IllegalStateException("Chest kind cannot be null.");

        this.chestKind = chestKind;
        chestInventory = new Inventory(Config.CHEST_INVENTORY_SIZE);
    }

    @Override
    public boolean canReceiveHit() {return chestKind == ChestKind.STANDARD;}
    public Inventory getChestInventory() {return chestInventory;}
    public ChestKind getKind() {return chestKind;}
}
