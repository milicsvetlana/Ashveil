package com.ashveil.save;

import com.ashveil.Config;
import com.ashveil.combat.Projectile;
import com.ashveil.combat.ProjectileSystem;
import com.ashveil.entities.Player;
import com.ashveil.entities.enemies.Enemy;
import com.ashveil.entities.enemies.EnemySpawnSystem;
import com.ashveil.entities.enemies.EnemyType;
import com.ashveil.farming.Crop;
import com.ashveil.farming.FarmingSystem;
import com.ashveil.farming.GrowablePlant;
import com.ashveil.farming.Sapling;
import com.ashveil.items.crafting.CraftingCategory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.objects.Chest;
import com.ashveil.objects.DestructibleObject;
import com.ashveil.progression.ProgressionState;
import com.ashveil.save.data.*;
import com.ashveil.world.DayNightCycle;
import com.ashveil.world.World;
import com.ashveil.world.WorldItem;

public class SaveMapper {

    public SaveData createSaveData(World world){
        SaveData saveData = new SaveData();

        saveData.saveVersion = SaveConstants.CURRENT_SAVE_VERSION;
        saveData.savedAt = System.currentTimeMillis();
        saveData.playTimeSeconds = world.getTotalPlayTimeSeconds();

        saveData.player = createPlayerSaveData(world.getPlayer());
        saveData.player.checkPointX = world.getCheckpointX();
        saveData.player.checkPointY = world.getCheckpointY();
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
        playerData.brokenHearts = player.getBrokenHearts();
        playerData.gold = player.getWallet().getGold();
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

    private AreaSaveData createAreaSaveData(World world){
        AreaSaveData areaSaveData = new AreaSaveData();
        areaSaveData.areaId = SaveConstants.MAIN_ISLAND_ID;

        for (DestructibleObject object : world.getDestructibleObjects()){
            areaSaveData.destructibleObjects.add(createDestructibleObjectSaveData(object));
        }

        addFarmingSaveData(world, areaSaveData);

        for (WorldItem groundItem : world.getGroundItems()){
            areaSaveData.groundItems.add(createWorldItemSaveData(groundItem));
        }

        for (Enemy enemy : world.getEnemies()){
            if (!enemy.isAlive()) continue;
            areaSaveData.enemies.add(createEnemySaveData(enemy));
        }

        for (Projectile projectile : world.getProjectileSystem().getProjectiles()){
            if (!projectile.isActive()) continue;
            areaSaveData.projectiles.add(createProjectileSaveData(projectile));
        }

        areaSaveData.nightSpawn = createNightSpawnSaveData(world.getEnemySpawnSystem());

        return areaSaveData;
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

    private PlantSaveData createPlantSaveData(GrowablePlant plant, int tileX, int tileY){
        PlantSaveData plantSaveData = new PlantSaveData();
        plantSaveData.tileX = tileX;
        plantSaveData.tileY = tileY;
        plantSaveData.growthTimer = plant.getGrowthTimer();

        if (plant instanceof Crop crop){
            plantSaveData.plantKind = "CROP";
            plantSaveData.cropType = crop.getCropType().name();
        }
        else if (plant instanceof Sapling){
            plantSaveData.plantKind = "SAPLING";
            plantSaveData.cropType = null;
        }
        else throw new IllegalStateException("Unsupported plant type " + plant.getClass().getSimpleName());

        return plantSaveData;
    }

    private void addFarmingSaveData(World world, AreaSaveData areaSaveData) {
        FarmingSystem farmingSystem = world.getFarmingSystem();

        for (int x = 0; x < farmingSystem.getWidth(); x++) {
            for (int y = 0; y < farmingSystem.getHeight(); y++) {
                if (farmingSystem.isTilled(x, y)) {
                    TilledTileSaveData tilledTileSaveData = new TilledTileSaveData();
                    tilledTileSaveData.tileX = x;
                    tilledTileSaveData.tileY = y;
                    areaSaveData.tilledTiles.add(tilledTileSaveData);
                }

                GrowablePlant plant = farmingSystem.getPlant(x, y);
                if (plant == null) continue;
                areaSaveData.plants.add(createPlantSaveData(plant, x, y));
            }
        }
    }

    private WorldItemSaveData createWorldItemSaveData(WorldItem worldItem){
        WorldItemSaveData worldItemSaveData = new WorldItemSaveData();
        worldItemSaveData.x = worldItem.getX();
        worldItemSaveData.y = worldItem.getY();
        worldItemSaveData.itemType = worldItem.getType().name();
        worldItemSaveData.quantity = worldItem.getStack().getQuantity();
        worldItemSaveData.durability = worldItem.getStack().getDurability();
        return worldItemSaveData;
    }

    private EnemySaveData createEnemySaveData(Enemy enemy){
        EnemySaveData enemySaveData = new EnemySaveData();
        enemySaveData.enemyType = enemy.getEnemyType().name();
        enemySaveData.x = enemy.getX();
        enemySaveData.y = enemy.getY();
        enemySaveData.currentHp = enemy.getCurrentHp();
        return enemySaveData;
    }

    private ProjectileSaveData createProjectileSaveData(Projectile projectile){
        ProjectileSaveData projectileSaveData = new ProjectileSaveData();
        projectileSaveData.x = projectile.getX();
        projectileSaveData.y = projectile.getY();
        projectileSaveData.velocityX = projectile.getVelocityX();
        projectileSaveData.velocityY = projectile.getVelocityY();
        projectileSaveData.damage = projectile.getDamage();
        projectileSaveData.remainingLifetime = projectile.getLifetime();
        return projectileSaveData;
    }

    private NightSpawnSaveData createNightSpawnSaveData(EnemySpawnSystem enemySpawnSystem){
        NightSpawnSaveData nightSpawnSaveData = new NightSpawnSaveData();
        nightSpawnSaveData.spawnTimer = enemySpawnSystem.getSpawnTimer();
        nightSpawnSaveData.spawnInterval = enemySpawnSystem.getSpawnInterval();

        for (EnemyType enemyType : enemySpawnSystem.getRemainingSpawnQueue()){
            nightSpawnSaveData.remainingQueue.add(enemyType.name());
        }

        return nightSpawnSaveData;
    }
}

