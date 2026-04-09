package com.ashveil.objects;

import com.ashveil.Config;
import com.ashveil.items.ItemType;

public enum ResourceType {
    TREE(Config.TREE_HP, ItemType.WOOD, 1, 4),
    ROCK(Config.ROCK_HP, ItemType.STONE, 1, 3);

    public final int hp;
    public final ItemType drop;
    public final int minDrop;
    public final int maxDrop;

    ResourceType(int hp, ItemType drop, int minDrop, int maxDrop){
        this.hp = hp;
        this.drop = drop;
        this.minDrop = minDrop;
        this.maxDrop = maxDrop;
    }
}
