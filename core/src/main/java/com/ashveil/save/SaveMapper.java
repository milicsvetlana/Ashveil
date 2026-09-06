package com.ashveil.save;

import com.ashveil.Config;
import com.ashveil.entities.Player;
import com.ashveil.items.crafting.CraftingCategory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.objects.Chest;
import com.ashveil.objects.DestructibleObject;
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

        saveData.currentAreaId = SaveConstants.MAIN_ISLAND_ID;
        saveData.areas.add(createAreaSaveData(world));

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

            playerData.inventory.add(createItemStackSaveData(itemStack, i));
        }
        return playerData;
    }

    private ItemStackSaveData createItemStackSaveData(ItemStack itemStack, int slot){
        ItemStackSaveData itemData = new ItemStackSaveData();
        itemData.slot = slot;
        itemData.itemType = itemStack.getType().name();
        itemData.quantity = itemStack.getQuantity();
        itemData.durability = itemStack.getDurability();
        return itemData;
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

    private DestructibleObjectSaveData createDestructibleObjectSaveData(DestructibleObject object){
        DestructibleObjectSaveData destructibleObjectSaveData = new DestructibleObjectSaveData();
        destructibleObjectSaveData.objectType = object.getType().name();
        destructibleObjectSaveData.x = object.getX();
        destructibleObjectSaveData.y = object.getY();
        destructibleObjectSaveData.currentHp = object.getCurrenthp();

        if (object instanceof Chest chest){
            for (int i=0; i < chest.getChestInventory().getSize(); i++){
                ItemStack itemStack = chest.getChestInventory().getSlot(i);
                if (itemStack == null) continue;
                destructibleObjectSaveData.chestInventory.add(createItemStackSaveData(itemStack, i));
            }
        }
        return destructibleObjectSaveData;
    }

    private AreaSaveData createAreaSaveData(World world){
        AreaSaveData areaSaveData = new AreaSaveData();
        areaSaveData.areaId = SaveConstants.MAIN_ISLAND_ID;

        for (DestructibleObject object : world.getDestructibleObjects()){
            areaSaveData.destructibleObjects.add(createDestructibleObjectSaveData(object));
        }
        return areaSaveData;
    }
}
