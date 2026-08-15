package com.ashveil.objects;

import com.ashveil.Config;
import com.ashveil.combat.HitCategory;
import com.ashveil.items.inventory.ItemType;

public enum DestructibleObjectType {
    TREE(Config.TREE_HP,1, 3, HitCategory.WOOD, true),
    ROCK(Config.ROCK_HP, 1, 3, HitCategory.STONE, true),
    FENCE(Config.FENCE_HP, 1, 1, HitCategory.WOOD, false);

    private final int hp;
    private final int minDrop;
    private final int maxDrop;
    private final HitCategory hitCategory;
    private final boolean spawnsNaturally;

    DestructibleObjectType(int hp, int minDrop, int maxDrop, HitCategory hitCategory, boolean spawnsNaturally){
        this.hp = hp;
        this.minDrop = minDrop;
        this.maxDrop = maxDrop;
        this.hitCategory = hitCategory;
        this.spawnsNaturally = spawnsNaturally;
    }

    public int getHp() {return hp;}
    public int getMinDrop() {return minDrop;}
    public int getMaxDrop() {return maxDrop;}
    public HitCategory getHitCategory() {return hitCategory;}
    public boolean spawnsNaturally() {return spawnsNaturally;}
}
