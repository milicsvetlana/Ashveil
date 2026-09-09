package com.ashveil.progression;

import com.ashveil.items.crafting.CraftingCategory;

import java.util.HashSet;
import java.util.Set;

public final class ProgressionState {
    private boolean firstTreeDropClaimed;
    private boolean wispNightUnlocked;
    private boolean wraithNightUnlocked;

    private final Set<CraftingCategory> unlockedCraftingCategories;

    public ProgressionState() {
        this.firstTreeDropClaimed = false;
        this.wispNightUnlocked = false;
        this.wraithNightUnlocked = false;

        this.unlockedCraftingCategories = new HashSet<>();
        unlockedCraftingCategories.add(CraftingCategory.WEAPONS);
        unlockedCraftingCategories.add(CraftingCategory.TOOLS);
        unlockedCraftingCategories.add(CraftingCategory.FOOD);
        unlockedCraftingCategories.add(CraftingCategory.BUILDING);
    }

    public void applyPersistentState(boolean firstTreeDropClaimed, boolean wispNightUnlocked,
                                     boolean wraithNightUnlocked, Set<CraftingCategory> unlockedCraftingCategories){
        if (unlockedCraftingCategories == null) throw new IllegalArgumentException("Unlocked crafting categories can't be null");
        this.firstTreeDropClaimed = firstTreeDropClaimed;
        this.wispNightUnlocked = wispNightUnlocked;
        this.wraithNightUnlocked = wraithNightUnlocked;

        this.unlockedCraftingCategories.clear();
        this.unlockedCraftingCategories.addAll(unlockedCraftingCategories);
    }



    public boolean isFirstTreeDropClaimed() {return firstTreeDropClaimed;}
    public void claimFirstTreeDrop() {firstTreeDropClaimed = true;}
    public boolean isWispNightUnlocked() {return wispNightUnlocked;}
    public boolean isWraithNightUnlocked() {return wraithNightUnlocked;}
    public void unlockWraithNight() {wraithNightUnlocked = true;}
    public boolean isCraftingCategoryUnlocked(CraftingCategory category) {return unlockedCraftingCategories.contains(category);}
    public void unlockCraftingCategory(CraftingCategory category) {unlockedCraftingCategories.add(category);}
    public Set<CraftingCategory> getUnlockedCraftingCategories() {return Set.copyOf(unlockedCraftingCategories);}
}
