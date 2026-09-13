package com.ashveil.save;

import com.ashveil.Config;
import com.ashveil.combat.Projectile;
import com.ashveil.combat.ProjectileSystem;
import com.ashveil.entities.Player;
import com.ashveil.entities.enemies.Enemy;
import com.ashveil.entities.enemies.EnemySpawnSystem;
import com.ashveil.entities.enemies.EnemyType;
import com.ashveil.farming.*;
import com.ashveil.items.crafting.CraftingCategory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.objects.Chest;
import com.ashveil.objects.DestructibleObject;
import com.ashveil.objects.DestructibleObjectType;
import com.ashveil.progression.ProgressionState;
import com.ashveil.save.data.*;
import com.ashveil.world.DayNightCycle;
import com.ashveil.world.DayPhase;
import com.ashveil.world.World;
import com.ashveil.world.WorldItem;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SaveMapper {

    public World createWorld(SaveData saveData){
        if (saveData == null) throw new IllegalArgumentException("SaveData can't be null.");

        World world = World.createForLoad();

        applyWorldState(world, saveData);
        applyPlayerState(world.getPlayer(), saveData.player);
        applyProgressionState(world.getProgressionState(), saveData.progressionState);
        applyDayNightState(world.getDayNightCycle(), saveData.dayNight);

        AreaSaveData currentArea = findCurrentArea(saveData);
        applyDestructibleObjectState(world, currentArea);

        applyFarmingState(world.getFarmingSystem(), currentArea);
        applyGroundItemState(world, currentArea);
        applyEnemyState(world, currentArea);
        applyProjectileState(world.getProjectileSystem(), currentArea);
        applyNightSpawnState(world.getEnemySpawnSystem(), currentArea.nightSpawn);

        return world;
    }

    private AreaSaveData findCurrentArea(SaveData saveData){
        for (AreaSaveData areaSaveData : saveData.areas){
            if (saveData.currentAreaId.equals(areaSaveData.areaId)) return areaSaveData;
        }
        throw new IllegalStateException("Current area not found in save data.");
    }

    public void applyDestructibleObjectState(World world, AreaSaveData areaSaveData){
        for (DestructibleObjectSaveData objectSaveData : areaSaveData.destructibleObjects){
            DestructibleObjectType type = DestructibleObjectType.valueOf(objectSaveData.objectType);
            DestructibleObject object = world.getDestructibleObjectSystem().createAndAdd(objectSaveData.x, objectSaveData.y,
                                                                                         type, objectSaveData.currentHp);
            if (object instanceof Chest chest){
                ItemStack[] chestContents = createInventoryContents(objectSaveData.chestInventory, Config.CHEST_INVENTORY_SIZE);
                chest.getChestInventory().replaceContents(chestContents);
            }
        }
    }

    private void applyWorldState(World world, SaveData saveData){
        world.applyPersistentState(saveData.player.checkPointX, saveData.player.checkPointY, saveData.playTimeSeconds);
    }

    private void applyPlayerState(Player player, PlayerSaveData playerSaveData){
        player.setPosition(playerSaveData.x, playerSaveData.y);

        ItemStack[] inventoryContents = createInventoryContents(playerSaveData.inventory, Config.INVENTORY_SIZE);
        player.applyPersistentState(playerSaveData.health, playerSaveData.brokenHearts, playerSaveData.gold,
                                    playerSaveData.selectedHotbarSlot, inventoryContents);
    }

    private void applyDayNightState(DayNightCycle dayNightCycle, DayNightSaveData dayNightSaveData){
        DayPhase phase = DayPhase.valueOf(dayNightSaveData.phase);
        dayNightCycle.applyPersistentState(dayNightSaveData.dayCount, phase, dayNightSaveData.phaseTimer);
    }

    private void applyProgressionState(ProgressionState progressionState, ProgressionSaveData progressionSaveData){
        EnumSet<CraftingCategory> unlockedCategories = EnumSet.noneOf(CraftingCategory.class);

        for (String categoryName : progressionSaveData.unlockedCraftingCategories){
            unlockedCategories.add(CraftingCategory.valueOf(categoryName));
        }

        progressionState.applyPersistentState(progressionSaveData.firstTreeDropClaimed, progressionSaveData.wispNightUnlocked,
                                              progressionSaveData.wraithNightUnlocked, unlockedCategories);
    }

    private void applyFarmingState(FarmingSystem farmingSystem, AreaSaveData areaSaveData){
        for (TilledTileSaveData tileData : areaSaveData.tilledTiles){
            farmingSystem.till(tileData.tileX, tileData.tileY);
        }

        for (PlantSaveData plantData : areaSaveData.plants){
            if ("CROP".equals(plantData.plantKind)){
                CropType cropType = CropType.valueOf(plantData.cropType);
                farmingSystem.plant(cropType, plantData.tileX, plantData.tileY);
            }
            else if ("SAPLING".equals(plantData.plantKind)){
                farmingSystem.plantSapling(plantData.tileX, plantData.tileY);
            }

            GrowablePlant plant = farmingSystem.getPlant(plantData.tileX, plantData.tileY);
            plant.restoreGrowthTimer(plantData.growthTimer);
        }
    }

    private void applyGroundItemState(World world, AreaSaveData areaSaveData){
        List<WorldItem> restoredItems = new ArrayList<>();
        for (WorldItemSaveData itemSaveData : areaSaveData.groundItems){
            ItemType itemType = ItemType.valueOf(itemSaveData.itemType);
            ItemStack itemStack = new ItemStack(itemType, itemSaveData.quantity, itemSaveData.durability);
            restoredItems.add(new WorldItem(itemSaveData.x, itemSaveData.y, itemStack));
        }
        world.getWorldItemSystem().replaceItems(restoredItems);
    }

    private void applyEnemyState(World world, AreaSaveData areaSaveData){
        for (EnemySaveData enemySaveData : areaSaveData.enemies){
            EnemyType enemyType = EnemyType.valueOf(enemySaveData.enemyType);
            world.getEnemySpawnSystem().createAndAddEnemy(enemyType, enemySaveData.x, enemySaveData.y, enemySaveData.currentHp);
        }
    }

    private void applyProjectileState(ProjectileSystem projectileSystem, AreaSaveData areaSaveData){
        List<Projectile> restoredProjectiles = new ArrayList<>();

        for (ProjectileSaveData projectileSaveData : areaSaveData.projectiles){
            restoredProjectiles.add(Projectile.fromVelocity(projectileSaveData.x, projectileSaveData.y, projectileSaveData.velocityX,
                                                             projectileSaveData.velocityY, projectileSaveData.damage, projectileSaveData.remainingLifetime));
        }
        projectileSystem.replaceProjectiles(restoredProjectiles);
    }

    private void applyNightSpawnState(EnemySpawnSystem enemySpawnSystem, NightSpawnSaveData nightSpawnSaveData){
        List<EnemyType> remainingQueue = new ArrayList<>();

        for (String enemyTypeName : nightSpawnSaveData.remainingQueue){
            remainingQueue.add(EnemyType.valueOf(enemyTypeName));
        }

        enemySpawnSystem.applyPersistentState(remainingQueue, nightSpawnSaveData.spawnTimer, nightSpawnSaveData.spawnInterval);
    }

    private ItemStack[] createInventoryContents(List<ItemStackSaveData> itemDataList, int inventorySize){
        ItemStack [] contents = new ItemStack[Config.INVENTORY_SIZE];
        for (ItemStackSaveData itemData : itemDataList){
            ItemType itemType = ItemType.valueOf(itemData.itemType);
            contents[itemData.slot] = new ItemStack(itemType, itemData.quantity, itemData.durability);
        }
        return contents;
    }

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

