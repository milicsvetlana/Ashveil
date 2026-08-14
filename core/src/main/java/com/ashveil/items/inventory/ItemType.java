package com.ashveil.items.inventory;

import com.ashveil.Config;
import com.ashveil.combat.DamageProfile;
import com.ashveil.combat.HitCategory;
import com.ashveil.targeting.TargetMode;

import java.util.Map;

public enum ItemType {

    WOOD(
        "Wood",
        "A basic crafting material gathered from trees.",
        20,
        0,
        true,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of()),
        TargetMode.NONE
    ),

    STONE(
        "Stone",
        "A durable crafting material gathered from rocks.",
        20,
        0,
        true,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of()),
        TargetMode.NONE
    ),

    FENCE(
        "Fence",
        "A wooden barrier used to protect and divide areas.",
        20,
        0,
        true,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of()),
        TargetMode.PLACE
    ),

    WHEAT(
        "Wheat",
        "A harvested crop used for preparing food.",
        20,
        0,
        true,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of()),
        TargetMode.NONE
    ),

    WHEAT_SEED(
        "Wheat Seed",
        "A seed that can be planted to grow wheat.",
        20,
        0,
        true,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of()),
        TargetMode.PLANT
    ),

    BREAD(
        "Bread",
        "A simple food made from wheat.",
        20,
        0,
        true,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of()),
        TargetMode.NONE
    ),

    WOODEN_AXE(
        "Wooden Axe",
        "A basic wooden tool used for chopping trees.",
        1,
        20,
        true,
        new DamageProfile(
            Config.PLAYER_BASE_DAMAGE,
            Map.of(
                HitCategory.WOOD, 3,
                HitCategory.ENTITY, 2,
                HitCategory.STONE, 0
            )
        ),
        TargetMode.NONE
    ),

    STONE_AXE(
        "Stone Axe",
        "A durable stone tool used for chopping trees more efficiently.",
        1,
        60,
        true,
        new DamageProfile(
            Config.PLAYER_BASE_DAMAGE,
            Map.of(
                HitCategory.WOOD, 5,
                HitCategory.ENTITY, 3,
                HitCategory.STONE, 0
            )
        ),
        TargetMode.NONE
    ),

    WOODEN_PICKAXE(
        "Wooden Pickaxe",
        "A basic wooden tool capable of breaking rocks.",
        1,
        20,
        true,
        new DamageProfile(
            Config.PLAYER_BASE_DAMAGE,
            Map.of(
                HitCategory.STONE, 3,
                HitCategory.ENTITY, 2,
                HitCategory.WOOD, 0
            )
        ),
        TargetMode.NONE
    ),

    STONE_PICKAXE(
        "Stone Pickaxe",
        "A durable stone tool used for breaking rocks more efficiently.",
        1,
        60,
        true,
        new DamageProfile(
            Config.PLAYER_BASE_DAMAGE,
            Map.of(
                HitCategory.STONE, 5,
                HitCategory.ENTITY, 3,
                HitCategory.WOOD, 0
            )
        ),
        TargetMode.NONE
    ),

    WOODEN_HOE(
        "Wooden Hoe",
        "A basic wooden farming tool used to prepare soil.",
        1,
        20,
        true,
        new DamageProfile(
            Config.PLAYER_BASE_DAMAGE,
            Map.of(
                HitCategory.ENTITY, 2,
                HitCategory.STONE, 0
            )
        ),
        TargetMode.TILL
    ),

    STONE_HOE(
        "Stone Hoe",
        "A durable stone farming tool used to prepare soil.",
        1,
        60,
        true,
        new DamageProfile(
            Config.PLAYER_BASE_DAMAGE,
            Map.of(
                HitCategory.ENTITY, 3,
                HitCategory.STONE, 0
            )
        ),
        TargetMode.TILL
    ),

    WOODEN_SWORD(
        "Wooden Sword",
        "A basic wooden weapon used against hostile entities.",
        1,
        20,
        true,
        new DamageProfile(
            Config.PLAYER_BASE_DAMAGE,
            Map.of(
                HitCategory.ENTITY, 3,
                HitCategory.STONE, 0
            )
        ),
        TargetMode.NONE
    ),

    STONE_SWORD(
        "Stone Sword",
        "A durable stone weapon that deals increased damage to hostile entities.",
        1,
        60,
        true,
        new DamageProfile(
            Config.PLAYER_BASE_DAMAGE,
            Map.of(
                HitCategory.ENTITY, 5,
                HitCategory.STONE, 0
            )
        ),
        TargetMode.NONE
    ),

    BOAT_KIT(
        "Boat Kit",
        "A set of parts used to assemble a boat at the old jetty.",
        1,
        0,
        false,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of()),
        TargetMode.NONE
    ),

    LORE_SCROLL(
        "Lore Scroll",
        "An ancient scroll containing fragments of forgotten knowledge.",
        1,
        0,
        false,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of()),
        TargetMode.NONE
    ),

    GOLD(
        "Gold",
        "Money",
        20,
        0,
        true,
        null,
        TargetMode.NONE
    ),

    // TEMP NAZIV, PROMENICU
    HEART_REPAIR(
        "Health Repair",
        "Mends a broken heart and restores your health to its current maximum.",
        3,
        0,
        true,
        new DamageProfile(Config.PLAYER_BASE_DAMAGE, Map.of()),
        TargetMode.NONE
    );

    ;

    private final String displayName;
    private final String description;
    private final int maxStack;
    private final int maxDurability;
    private boolean despawnsOnGround;
    private final DamageProfile damageProfile;
    private final TargetMode targetMode;

    ItemType(String displayName, String description, int maxStack, int maxDurability, boolean despawnsOnGround,
             DamageProfile damageProfile, TargetMode targetMode){
        this.displayName = displayName;
        this.description = description;
        this.maxStack = maxStack;
        this.maxDurability = maxDurability;
        this.despawnsOnGround = despawnsOnGround;
        this.damageProfile = damageProfile;
        this.targetMode = targetMode;
    }

    public String getDisplayName() {return displayName;}
    public String getDescription() {return description;}
    public int getMaxStack() {return maxStack;}
    public int getMaxDurability() {return maxDurability;}
    public DamageProfile getDamageProfile() {
        return damageProfile;
    }
    public TargetMode getTargetMode() {return targetMode;}

    public boolean isStackable(){return maxStack > 1;}
    public boolean usesDurability(){return maxDurability > 0;}
    public boolean despawnsOnGround() {return despawnsOnGround;}
}
