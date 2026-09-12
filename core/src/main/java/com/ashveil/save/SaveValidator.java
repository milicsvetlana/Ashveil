package com.ashveil.save;

import com.ashveil.Config;
import com.ashveil.entities.enemies.EnemyType;
import com.ashveil.farming.CropType;
import com.ashveil.items.crafting.CraftingCategory;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.objects.DestructibleObjectType;
import com.ashveil.save.data.*;
import com.ashveil.world.DayNightCycle;
import com.ashveil.world.DayPhase;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SaveValidator {
    public boolean isValid(SaveData saveData) {
        if (saveData == null) return false;
        if (saveData.saveVersion != SaveConstants.CURRENT_SAVE_VERSION) return false;

        if (!playerIsValid(saveData.player)) return false;
        if (!progressionIsValid(saveData.progressionState)) return false;
        if (!dayNightIsValid(saveData.dayNight)) return false;
        if (saveData.currentAreaId == null) return false;
        if (saveData.areas == null) return false;

        boolean currentAreaFound = false;
        for (AreaSaveData areaSaveData : saveData.areas) {
            if (!areaIsValid(areaSaveData)) return false;
            if (saveData.currentAreaId.equals(areaSaveData.areaId)) currentAreaFound = true;
        }

        return currentAreaFound;
    }

    private boolean playerIsValid(PlayerSaveData playerSaveData) {
        if (playerSaveData == null) return false;
        if (playerSaveData.inventory == null) return false;

        if (playerSaveData.brokenHearts < 0 || playerSaveData.brokenHearts > Config.MAX_BROKEN_HEARTS) return false;
        int restoredMaxHp = Config.PLAYER_HP - playerSaveData.brokenHearts * Config.HP_PER_HEART;
        if (playerSaveData.health < 0 || playerSaveData.health > restoredMaxHp) return false;

        if (playerSaveData.gold < 0) return false;
        if (0 > playerSaveData.selectedHotbarSlot || playerSaveData.selectedHotbarSlot >= Config.HOTBAR_SIZE)
            return false;

        return inventoryIsValid(playerSaveData.inventory, Config.INVENTORY_SIZE);
    }

    private boolean inventoryIsValid(List<ItemStackSaveData> inventory, int inventorySize) {
        if (inventory == null) return false;
        if (inventory.size() > inventorySize) return false;

        boolean[] occupiedSlots = new boolean[inventorySize];
        for (ItemStackSaveData itemStackSaveData : inventory) {
            if (!inventoryItemIsValid(itemStackSaveData, occupiedSlots)) return false;
        }
        return true;
    }

    private boolean inventoryItemIsValid(ItemStackSaveData itemStackSaveData, boolean[] occupiedSlots) {
        if (itemStackSaveData == null) return false;
        if (itemStackSaveData.slot < 0 || itemStackSaveData.slot >= occupiedSlots.length) return false;
        if (occupiedSlots[itemStackSaveData.slot]) return false;

        occupiedSlots[itemStackSaveData.slot] = true;
        if (itemStackSaveData.itemType == null) return false;

        ItemType itemType;
        try {
            itemType = ItemType.valueOf(itemStackSaveData.itemType);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        if (itemStackSaveData.quantity <= 0 || itemStackSaveData.quantity > itemType.getMaxStack()) return false;
        return itemStackSaveData.durability >= 0 && itemStackSaveData.durability <= itemType.getMaxDurability();
    }

    private boolean progressionIsValid(ProgressionSaveData progressionSaveData) {
        if (progressionSaveData == null) return false;
        if (progressionSaveData.unlockedCraftingCategories == null) return false;

        EnumSet<CraftingCategory> unlockedCategories = EnumSet.noneOf(CraftingCategory.class);
        for (String categoryName : progressionSaveData.unlockedCraftingCategories) {
            if (categoryName == null) return false;

            CraftingCategory craftingCategory;
            try {
                craftingCategory = CraftingCategory.valueOf(categoryName);
            } catch (IllegalArgumentException exception) {
                return false;
            }

            if (!unlockedCategories.add(craftingCategory)) return false;
        }

        return unlockedCategories.contains(CraftingCategory.WEAPONS) &&
            unlockedCategories.contains(CraftingCategory.TOOLS) &&
            unlockedCategories.contains(CraftingCategory.FOOD) &&
            unlockedCategories.contains(CraftingCategory.BUILDING);
    }

    private boolean dayNightIsValid(DayNightSaveData dayNightSaveData) {
        if (dayNightSaveData == null) return false;
        if (dayNightSaveData.dayCount < 1) return false;
        if (dayNightSaveData.phase == null) return false;

        DayPhase phase;
        try {
            phase = DayPhase.valueOf(dayNightSaveData.phase);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        float phaseDuration = DayNightCycle.getPhaseDuration(phase, dayNightSaveData.dayCount);
        return !(dayNightSaveData.phaseTimer < 0) && !(dayNightSaveData.phaseTimer >= phaseDuration);
    }

    private boolean destructibleObjectIsValid(DestructibleObjectSaveData destructibleObjectSaveData) {
        if (destructibleObjectSaveData == null) return false;
        if (destructibleObjectSaveData.objectType == null) return false;

        DestructibleObjectType type;

        try {
            type = DestructibleObjectType.valueOf(destructibleObjectSaveData.objectType);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        if (destructibleObjectSaveData.currentHp <= 0 || destructibleObjectSaveData.currentHp > type.getHp())
            return false;

        if (type == DestructibleObjectType.CHEST) {
            return inventoryIsValid(destructibleObjectSaveData.chestInventory, Config.CHEST_INVENTORY_SIZE);
        }

        return destructibleObjectSaveData.chestInventory != null && destructibleObjectSaveData.chestInventory.isEmpty();
    }

    private boolean areaIsValid(AreaSaveData areaSaveData) {
        if (areaSaveData == null || areaSaveData.areaId == null || areaSaveData.destructibleObjects == null ||
            areaSaveData.groundItems == null || !nightSpawnIsValid(areaSaveData.nightSpawn) ||
            !farmingIsValid(areaSaveData)
        ) return false;

        for (DestructibleObjectSaveData objectData : areaSaveData.destructibleObjects) {
            if (!destructibleObjectIsValid(objectData)) return false;
        }

        for (EnemySaveData enemySaveData : areaSaveData.enemies) {
            if (!enemyIsValid(enemySaveData)) return false;
        }

        for (ProjectileSaveData projectileSaveData : areaSaveData.projectiles){
            if (!projectileIsValid(projectileSaveData)) return false;
        }

        return true;
    }

    private boolean projectileIsValid(ProjectileSaveData projectileSaveData) {
        if (projectileSaveData == null) return false;
        if (Float.isNaN(projectileSaveData.x) || Float.isInfinite(projectileSaveData.x)
            || Float.isNaN(projectileSaveData.y) || Float.isInfinite(projectileSaveData.y)) return false;

        if (Float.isNaN(projectileSaveData.velocityX) || Float.isInfinite(projectileSaveData.velocityX)
            || Float.isNaN(projectileSaveData.velocityY) || Float.isInfinite(projectileSaveData.velocityY))
            return false;

        if (projectileSaveData.damage <= 0) return false;

        if (Float.isNaN(projectileSaveData.remainingLifetime) || Float.isInfinite(projectileSaveData.remainingLifetime)
            || projectileSaveData.remainingLifetime <= 0) return false;

        return true;
    }

    private String createTileKey(int tileX, int tileY){
        return tileX + ":" + tileY;
    }

    private boolean farmingIsValid(AreaSaveData areaSaveData){
        if (areaSaveData.tilledTiles == null) return false;
        if (areaSaveData.plants == null) return false;

        Set<String> tilledPositions = new HashSet<>();

        for (TilledTileSaveData tileSaveData : areaSaveData.tilledTiles){
            if (tileSaveData == null) return false;
            if (tileSaveData.tileX < 0 || tileSaveData.tileY < 0) return false;

            String tileKey = createTileKey(tileSaveData.tileX, tileSaveData.tileY);
            if (!tilledPositions.add(tileKey)) return false;
        }

        Set<String> plantPositions = new HashSet<>();

        for (PlantSaveData plantSaveData : areaSaveData.plants){
            if (plantSaveData == null) return false;
            if (plantSaveData.tileX < 0 || plantSaveData.tileY < 0) return false;

            String tileKey = createTileKey(plantSaveData.tileX, plantSaveData.tileY);
            if (!plantPositions.add(tileKey)) return false;
            if (plantSaveData.plantKind == null) return false;
            if (plantSaveData.growthTimer < 0) return false;

            if ("CROP".equals(plantSaveData.plantKind)){
                if (!cropPlantIsValid(plantSaveData, tilledPositions)) return false;
            }
            else if ("SAPLING".equals(plantSaveData.plantKind)){
                if (plantSaveData.cropType != null) return false;
                if (tilledPositions.contains(tileKey)) return false;
            }
            else return false;
        }
        return true;
    }

    private boolean cropPlantIsValid(PlantSaveData plantSaveData, Set<String> tilledPositions){
        if (plantSaveData.cropType == null) return false;
        try{
            CropType.valueOf(plantSaveData.cropType);
        }
        catch (IllegalArgumentException exception){
            return false;
        }

        //biljku mozemo samo na tilled polju
        String tileKey = createTileKey(plantSaveData.tileX, plantSaveData.tileY);
        return tilledPositions.contains(tileKey);
    }

    private boolean worldItemIsValid(WorldItemSaveData itemData) {
        if (itemData == null) return false;
        if (Float.isNaN(itemData.x) || Float.isInfinite(itemData.y) || Float.isInfinite(itemData.y)) return false;
        if (itemData.itemType == null) return false;

        ItemType itemType;
        try{
            itemType = ItemType.valueOf(itemData.itemType);
        }
        catch (IllegalArgumentException exception) {
            return false;
        }

        if (itemData.quantity <= 0 || itemData.quantity > itemType.getMaxStack()) return false;
        if (itemData.durability < 0 || itemData.durability > itemType.getMaxDurability()) return false;
        return true;
    }

    private boolean enemyIsValid(EnemySaveData enemyData){
        if (enemyData == null) return false;
        if (enemyData.enemyType == null) return false;

        if (Float.isNaN(enemyData.x) || Float.isInfinite(enemyData.x) || Float.isNaN(enemyData.y) || Float.isInfinite(enemyData.y))
            return false;

        EnemyType enemyType;
        try {
            enemyType = EnemyType.valueOf(enemyData.enemyType);
        }
        catch (IllegalArgumentException exception){
            return false;
        }

        if (enemyData.currentHp <= 0 || enemyData.currentHp > enemyType.getMaxHp()) return false;
        return true;
    }

    private boolean nightSpawnIsValid(NightSpawnSaveData nightSpawnSaveData){
        if (nightSpawnSaveData == null) return false;
        if (nightSpawnSaveData.remainingQueue == null) return false;

        if (Float.isNaN(nightSpawnSaveData.spawnTimer) || Float.isInfinite(nightSpawnSaveData.spawnTimer)
            || nightSpawnSaveData.spawnTimer < 0) return false;

        if (Float.isNaN(nightSpawnSaveData.spawnInterval) || Float.isInfinite(nightSpawnSaveData.spawnInterval)
            || nightSpawnSaveData.spawnInterval < 0) return false;

        if (!nightSpawnSaveData.remainingQueue.isEmpty() && nightSpawnSaveData.spawnInterval <= 0) return false;

        for (String enemyTypeName : nightSpawnSaveData.remainingQueue){
            if (enemyTypeName == null) return false;
            try {
                EnemyType.valueOf(enemyTypeName);
            }
            catch (IllegalArgumentException exception) {
                return false;
            }
        }

        return true;
    }

}
