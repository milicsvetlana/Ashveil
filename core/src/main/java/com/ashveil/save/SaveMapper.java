package com.ashveil.save;

import com.ashveil.Config;
import com.ashveil.entities.Player;
import com.ashveil.items.crafting.CraftingCategory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.progression.ProgressionState;
import com.ashveil.save.data.*;
import com.ashveil.world.DayNightCycle;
import com.ashveil.world.World;

public class SaveMapper {

    public SaveData createSaveData(World world){
        SaveData saveData = new SaveData();

        saveData.saveVersion = SaveConstants.CURRENT_SAVE_VERSION;
        saveData.savedAt = System.currentTimeMillis();
        saveData.playTimeSeconds = world.getTotalPlayTimeSeconds();

        saveData.player = createPlayerSaveData(world.getPlayer());
        saveData.dayNight = createDayNightSaveData(world.getDayNightCycle());
        saveData.progressionState = createProgressionSaveData(world.getProgressionState());

        return saveData;
    }

    public PlayerSaveData createPlayerSaveData(Player player){
        PlayerSaveData playerData = new PlayerSaveData();
        playerData.x = player.getX();
        playerData.y = player.getY();
        playerData.health = player.getCurrentHp();
        playerData.selectedHotbarSlot = player.getSelectedHotbarSlot();
        for (int i=0; i < Config.INVENTORY_SIZE; i++){
            ItemStack itemStack = player.getInventory().getSlot(i);
            if (itemStack == null) continue;

            ItemStackSaveData itemData = new ItemStackSaveData();
            itemData.slot = i;
            itemData.itemType = itemStack.getType().name();
            itemData.quantity = itemStack.getQuantity();
            itemData.durability = itemStack.getDurability();

            playerData.inventory.add(itemData);
        }
        return playerData;
    }

    public DayNightSaveData createDayNightSaveData(DayNightCycle dayNightCycle){
        DayNightSaveData dayNightSaveData = new DayNightSaveData();

        dayNightSaveData.dayCount = dayNightCycle.getDayCount();
        dayNightSaveData.phase = dayNightCycle.getDayPhase().name();
        dayNightSaveData.phaseTimer = dayNightCycle.getPhaseTimer();

        return dayNightSaveData;
    }

    private ProgressionSaveData createProgressionSaveData(ProgressionState progressionState){
        ProgressionSaveData progressionSaveData = new ProgressionSaveData();
        progressionSaveData.firstTreeDropClaimed = progressionState.isFirstTreeDropClaimed();
        progressionSaveData.wispNightUnlocked = progressionState.isWispNightUnlocked();
        progressionSaveData.wraithNightUnlocked = progressionState.isWraithNightUnlocked();

        for (CraftingCategory category : progressionState.getUnlockedCraftingCategories()){
            progressionSaveData.unlockedCraftingCategories.add(category.name());
        }

        return progressionSaveData;
    }

}
