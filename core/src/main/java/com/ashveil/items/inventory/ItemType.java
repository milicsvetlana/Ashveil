package com.ashveil.items.inventory;

import com.ashveil.Config;
import com.ashveil.combat.DamageProfile;
import com.ashveil.combat.HitCategory;

import java.util.Map;

public enum ItemType {
    WOOD(
        "Wood",
        "A basic crafting material gathered from trees.",
        20,
        0,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of())
    ),

    STONE(
        "Stone",
        "A durable crafting material gathered from rocks.",
        20,
        0,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of())
    ),

    FENCE(
        "Fence",
        "A wooden barrier used to protect and divide areas.",
        20,
        0,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of())
    ),

    WHEAT(
        "Wheat",
        "A harvested crop used for preparing food.",
        20,
        0,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of())
    ),

    WHEAT_SEED(
        "Wheat Seed",
        "A seed that can be planted to grow wheat.",
        20,
        0,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of())
    ),

    BREAD(
        "Bread",
        "A simple food made from wheat.",
        20,
        0,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of())
    ),

    AXE(
        "Axe",
        "A tool designed for chopping trees and wooden objects.",
        1,
        20,
        new DamageProfile(
            Config.PLAYER_BASE_DAMAGE,
            Map.of(
                HitCategory.WOOD, 3,
                HitCategory.ENTITY, 2,
                HitCategory.STONE, 0
            )
        )
    ),

    PICKAXE(
        "Pickaxe",
        "A tool designed for breaking stone and mining hard materials.",
        1,
        20,
        new DamageProfile(
            Config.PLAYER_BASE_DAMAGE,
            Map.of(
                HitCategory.STONE, 3,
                HitCategory.ENTITY, 2,
                HitCategory.WOOD, 0
            )
        )
    ),

    HOE(
        "Hoe",
        "A farming tool used to prepare soil for planting.",
        1,
        20,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of(HitCategory.ENTITY, 2))
    ),

    SWORD(
        "Sword",
        "A weapon designed for fighting hostile entities.",
        1,
        20,
        new DamageProfile(
            Config.PLAYER_BASE_DAMAGE,
            Map.of(
                HitCategory.ENTITY, 3
            )
        )
    ),

    BOAT_KIT(
        "Boat Kit",
        "A set of parts used to assemble a boat at the old jetty.",
        1,
        0,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of())
    ),

    LORE_SCROLL(
        "Lore Scroll",
        "An ancient scroll containing fragments of forgotten knowledge.",
        1,
        0,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of())
    );

    private final String displayName;
    private final String description;
    private final int maxStack;
    private final int maxDurability;
    private final DamageProfile damageProfile;

    ItemType(String displayName, String description, int maxStack, int maxDurability, DamageProfile damageProfile){
        this.displayName = displayName;
        this.description = description;
        this.maxStack = maxStack;
        this.maxDurability = maxDurability;
        this.damageProfile = damageProfile;
    }

    public String getDisplayName() {return displayName;}
    public String getDescription() {return description;}
    public int getMaxStack() {return maxStack;}
    public int getMaxDurability() {return maxDurability;}
    public DamageProfile getDamageProfile() {
        return damageProfile;
    }

    public boolean isStackable(){return maxStack > 1;}
    public boolean usesDurability(){return maxDurability > 0;}
}
