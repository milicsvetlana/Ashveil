package com.ashveil.progression;

import com.ashveil.items.crafting.CraftingCategory;
import com.ashveil.world.area.AreaID;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public final class ProgressionState {
    private boolean firstTreeDropClaimed;
    private boolean wispNightUnlocked;
    private boolean wraithNightUnlocked;
    private boolean boatKitCrafted;
    private boolean boatBuilt;
    private final EnumSet<AreaID> unlockedAreas;
    private boolean foundOldJetty;

    private boolean scrollIRead;
    private boolean scrollIIRead;
    private boolean scrollIIIRead;

    private final Set<CraftingCategory> unlockedCraftingCategories;

    private boolean windyWardCleared;
    private boolean dashUnlocked;

    public ProgressionState() {
        this.firstTreeDropClaimed = false;
        this.wispNightUnlocked = false;
        this.wraithNightUnlocked = false;
        boatKitCrafted = false;
        boatBuilt = false;
        unlockedAreas = EnumSet.of(AreaID.MAIN_ISLAND, AreaID.WINDY_PLAINS);
        foundOldJetty = false;

        this.unlockedCraftingCategories = new HashSet<>();
        unlockedCraftingCategories.add(CraftingCategory.WEAPONS);
        unlockedCraftingCategories.add(CraftingCategory.TOOLS);
        unlockedCraftingCategories.add(CraftingCategory.FOOD);
        unlockedCraftingCategories.add(CraftingCategory.BUILDING);

        windyWardCleared = false;
        dashUnlocked = false;
    }

    public void applyPersistentState(boolean firstTreeDropClaimed, boolean wispNightUnlocked,
                                     boolean wraithNightUnlocked, boolean boatKitCrafted, boolean boatBuilt,
                                     boolean foundOldJetty, boolean scrollIRead, boolean scrollIIRead, boolean scrollIIIRead,
                                     Set<CraftingCategory> unlockedCraftingCategories, Set<AreaID> unlockedAreas,
                                     boolean windyWardCleared, boolean dashUnlocked){
        if (unlockedCraftingCategories == null) throw new IllegalArgumentException("Unlocked crafting categories can't be null");
        if (unlockedAreas == null) throw new IllegalArgumentException("Unlocked areas can't be null");
        this.firstTreeDropClaimed = firstTreeDropClaimed;
        this.wispNightUnlocked = wispNightUnlocked;
        this.wraithNightUnlocked = wraithNightUnlocked;

        this.boatKitCrafted = boatKitCrafted;
        this.boatBuilt = boatBuilt;
        this.foundOldJetty = foundOldJetty;

        this.scrollIRead = scrollIRead;
        this.scrollIIRead = scrollIIRead;
        this.scrollIIIRead = scrollIIIRead;

        this.unlockedCraftingCategories.clear();
        this.unlockedCraftingCategories.addAll(unlockedCraftingCategories);

        this.unlockedAreas.clear();
        this.unlockedAreas.addAll(unlockedAreas);

        this.windyWardCleared = windyWardCleared;
        this.dashUnlocked = dashUnlocked;
    }

    public boolean isFirstTreeDropClaimed() {return firstTreeDropClaimed;}
    public void claimFirstTreeDrop() {firstTreeDropClaimed = true;}
    public boolean isWispNightUnlocked() {return wispNightUnlocked;}
    public void unlockWispNight(){wispNightUnlocked = true;}
    public boolean isWraithNightUnlocked() {return wraithNightUnlocked;}
    public void unlockWraithNight() {wraithNightUnlocked = true;}
    public boolean isCraftingCategoryUnlocked(CraftingCategory category) {return unlockedCraftingCategories.contains(category);}
    public void unlockCraftingCategory(CraftingCategory category) {unlockedCraftingCategories.add(category);}
    public Set<CraftingCategory> getUnlockedCraftingCategories() {return Set.copyOf(unlockedCraftingCategories);}
    public boolean isBoatBuilt() {return boatBuilt;}
    public void buildBoat() {boatBuilt = true;}
    public boolean isAreaUnlocked(AreaID areaId) {
        if (areaId == null) return false;
        return unlockedAreas.contains(areaId);
    }
    public void unlockArea(AreaID areaId) {
        if (areaId == null) throw new IllegalArgumentException("Area id cannot be null.");
        unlockedAreas.add(areaId);
    }
    public boolean isOldJettyFound(){return foundOldJetty;}
    public void discoverOldJetty(){foundOldJetty = true;}
    public boolean isBoatKitCrafted() {return boatKitCrafted;}
    public void markBoatKitCrafted() {boatKitCrafted = true;}

    public boolean isScrollIRead() {return scrollIRead;}
    public boolean isScrollIIRead() {return scrollIIRead;}
    public boolean isScrollIIIRead() {return scrollIIIRead;}
    public void markScrollIRead() {scrollIRead = true;}
    public void markScrollIIRead() {scrollIIRead = true;}
    public void markScrollIIIRead() {scrollIIIRead = true;}

    public boolean isWindyWardCleared(){return windyWardCleared;}
    public void clearWindyWard(){windyWardCleared = true;}
    public boolean isDashUnlocked(){return dashUnlocked;}
    public void unlockDash(){dashUnlocked = true;}
}
