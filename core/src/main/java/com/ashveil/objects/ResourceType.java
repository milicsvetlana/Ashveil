package com.ashveil.objects;

import com.ashveil.Config;
import com.ashveil.combat.HitCategory;
import com.ashveil.items.inventory.ItemType;

public enum ResourceType {
    TREE(Config.TREE_HP, ItemType.WOOD, 1, 3, HitCategory.WOOD),
    ROCK(Config.ROCK_HP, ItemType.STONE, 1, 3, HitCategory.STONE);

    private final int hp;
    private final ItemType drop;
    private final int minDrop;
    private final int maxDrop;
    private final HitCategory hitCategory;

    ResourceType(int hp, ItemType drop, int minDrop, int maxDrop, HitCategory hitCategory){
        this.hp = hp;
        this.drop = drop;
        this.minDrop = minDrop;
        this.maxDrop = maxDrop;
        this.hitCategory = hitCategory;
    }

    public int getHp() {return hp;}
    public ItemType getDrop() {return drop;}
    public int getMinDrop() {return minDrop;}
    public int getMaxDrop() {return maxDrop;}
    public HitCategory getHitCategory() {return hitCategory;}
}
