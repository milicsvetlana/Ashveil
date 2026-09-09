package com.ashveil.save;

import com.ashveil.Config;
import com.ashveil.items.crafting.CraftingCategory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.save.data.*;
import com.ashveil.world.DayNightCycle;
import com.ashveil.world.DayPhase;

import java.util.EnumSet;

public class SaveValidator {
    public boolean isValid(SaveData saveData){
        if (saveData == null) return false;
        if (saveData.saveVersion != SaveConstants.CURRENT_SAVE_VERSION) return false;

        if (!playerIsValid(saveData.player)) return false;
        if (!progressionIsValid(saveData.progressionState)) return false;
        if (!dayNightIsValid(saveData.dayNight)) return false;
        if (saveData.currentAreaId == null) return false;
        if (saveData.areas == null) return false;

        return true;
    }

    private boolean playerIsValid(PlayerSaveData playerSaveData){
        if (playerSaveData == null) return false;
        if (playerSaveData.inventory == null) return false;

        if (playerSaveData.brokenHearts < 0 || playerSaveData.brokenHearts > Config.MAX_BROKEN_HEARTS) return false;
        int restoredMaxHp = Config.PLAYER_HP - playerSaveData.brokenHearts * Config.HP_PER_HEART;
        if (playerSaveData.health < 0 || playerSaveData.health > restoredMaxHp) return false;

        if (playerSaveData.gold < 0) return false;
        if (0 > playerSaveData.selectedHotbarSlot || playerSaveData.selectedHotbarSlot >= Config.HOTBAR_SIZE) return false;

        boolean[] occupiedSlots = new boolean[Config.INVENTORY_SIZE];
        for (ItemStackSaveData itemData : playerSaveData.inventory){
            if (!inventoryItemIsValid(itemData, occupiedSlots)) return false;
        }

        return true;
    }

    private boolean inventoryItemIsValid(ItemStackSaveData itemStackSaveData, boolean[] occupiedSlots){
        if (itemStackSaveData == null) return false;
        if (itemStackSaveData.slot < 0 || itemStackSaveData.slot >= Config.INVENTORY_SIZE) return false;
        if (occupiedSlots[itemStackSaveData.slot]) return false;

        occupiedSlots[itemStackSaveData.slot] = true;
        if (itemStackSaveData.itemType == null) return false;

        ItemType itemType;
        try{
            itemType = ItemType.valueOf(itemStackSaveData.itemType);
        }
        catch (IllegalArgumentException exception){
            return false;
        }

        if (itemStackSaveData.quantity <= 0 || itemStackSaveData.quantity > itemType.getMaxStack()) return false;
        return (itemStackSaveData.durability < 0 || itemStackSaveData.durability > itemType.getMaxDurability());
    }

    private boolean progressionIsValid(ProgressionSaveData progressionSaveData){
        if (progressionSaveData == null) return false;
        if (progressionSaveData.unlockedCraftingCategories == null) return false;

        EnumSet<CraftingCategory> unlockedCategories = EnumSet.noneOf(CraftingCategory.class);
        for (String categoryName : progressionSaveData.unlockedCraftingCategories){
            if (categoryName == null) return false;

            CraftingCategory craftingCategory;
            try{
                craftingCategory = CraftingCategory.valueOf(categoryName);
            }
            catch (IllegalArgumentException exception){
                return false;
            }

            if (!unlockedCategories.add(craftingCategory)) return false;
        }

        return unlockedCategories.contains(CraftingCategory.WEAPONS) &&
            unlockedCategories.contains(CraftingCategory.TOOLS) &&
            unlockedCategories.contains(CraftingCategory.FOOD) &&
            unlockedCategories.contains(CraftingCategory.BUILDING);
    }

    private boolean dayNightIsValid(DayNightSaveData dayNightSaveData){
        if (dayNightSaveData == null) return false;
        if (dayNightSaveData.dayCount < 1) return false;
        if (dayNightSaveData.phase == null) return false;

        DayPhase phase;
        try{
            phase = DayPhase.valueOf(dayNightSaveData.phase);
        }
        catch (IllegalArgumentException exception){
            return false;
        }

        float phaseDuration = DayNightCycle.getPhaseDuration(phase, dayNightSaveData.dayCount);
        return !(dayNightSaveData.phaseTimer < 0) && !(dayNightSaveData.phaseTimer >= phaseDuration);
    }

}
