package com.ashveil.items.inventory;

public enum ItemType {
    WOOD("Wood", "A basic crafting material gathered from trees.", 20, 0),
    STONE("Stone", "A durable material gathered from rocks.", 20, 0),
    FENCE("Fence", "", 20, 0),
    WHEAT("Wheat", "", 20, 0),
    WHEAT_SEED("Wheat Seed", "", 20, 0),
    BREAD("Bread", "", 20, 0),
    AXE("Axe", "A tool used for chopping trees.", 1, 20),
    PICKAXE("Pickaxe", "", 1, 20),
    HOE("Hoe", "", 1, 20),
    SWORD("Sword", "", 1, 20),
    BOAT_KIT("Boat Kit", "A set of parts used to assemble a boat at the old jetty.", 1, 1),
    LORE_SCROLL("Lore Scroll", "", 1, 0),
    ;

    private final String displayName;
    private final String description;
    private final int maxStack;
    private final int maxDurability;

    ItemType(String displayName, String description, int maxStack, int maxDurability){
        this.displayName = displayName;
        this.description = description;
        this.maxStack = maxStack;
        this.maxDurability = maxDurability;
    }

    public String getDisplayName() {return displayName;}
    public String getDescription() {return description;}
    public int getMaxStack() {return maxStack;}
    public int getMaxDurability() {return maxDurability;}

    public boolean isStackable(){return maxStack > 1;}
    public boolean usesDurability(){return maxDurability > 0;}
}
