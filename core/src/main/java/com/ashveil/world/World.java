package com.ashveil.world;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.combat.CombatSystem;
import com.ashveil.combat.Hittable;
import com.ashveil.combat.ProjectileSystem;
import com.ashveil.entities.enemies.*;
import com.ashveil.entities.Player;
import com.ashveil.farming.*;
import com.ashveil.items.crafting.CraftStatus;
import com.ashveil.items.crafting.CraftingManager;
import com.ashveil.items.crafting.CraftingResult;
import com.ashveil.items.crafting.Recipe;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.navigation.DistanceField;
import com.ashveil.objects.Chest;
import com.ashveil.objects.DestructibleObject;
import com.ashveil.objects.DestructibleObjectSystem;
import com.ashveil.objects.DestructibleObjectType;
import com.ashveil.input.PlayerInput;
import com.ashveil.progression.ProgressionState;
import com.ashveil.items.crafting.CraftingAccess;
import com.ashveil.targeting.TargetMode;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World implements CraftingAccess {

    private final Random random = new Random();

    private final TileMap tileMap;
    private final DayNightCycle dayNightCycle;

    private final CraftingManager craftingManager;
    private final CombatSystem combatSystem;
    private final WorldItemSystem worldItemSystem;

    private final CollisionSystem collisionSystem;

    private final Player player;
    private final List<Enemy> enemies;

    private float checkpointX;
    private float checkpointY;

    private final ProgressionState progressionState;
    private TargetMode targetMode;
    private Chest activeChest;
    private Rectangle targetBounds;
    private final FarmingSystem farmingSystem;
    private final EnemySpawnSystem enemySpawnSystem;

    private final DistanceField distanceField;
    private final ProjectileSystem projectileSystem;
    private final DestructibleObjectSystem destructibleObjectSystem;

    private double totalPlayTimeSeconds;

    public World(){
        this(true);
    }

    private World(boolean initializeNewGame){
        tileMap = new TileMap();
        collisionSystem = new CollisionSystem(tileMap);
        player = new Player(tileMap.getPlayerSpawnX(), tileMap.getPlayerSpawnY(), tileMap, collisionSystem);
        checkpointX = tileMap.getPlayerSpawnX();
        checkpointY = tileMap.getPlayerSpawnY();
        enemies = new ArrayList<>();
        worldItemSystem = new WorldItemSystem();
        combatSystem = new CombatSystem();
        progressionState = new ProgressionState();
        craftingManager = new CraftingManager(progressionState);
        dayNightCycle = new DayNightCycle();
        targetMode = TargetMode.NONE;
        targetBounds = new Rectangle();
        activeChest = null;
        farmingSystem = new FarmingSystem(tileMap.getWidth(), tileMap.getHeight());
        distanceField = new DistanceField(tileMap, collisionSystem);
        projectileSystem = new ProjectileSystem(player, collisionSystem);
        enemySpawnSystem = new EnemySpawnSystem(progressionState, player, tileMap, collisionSystem, distanceField, enemies, projectileSystem);
        totalPlayTimeSeconds = 0;

        destructibleObjectSystem = new DestructibleObjectSystem(tileMap, collisionSystem, progressionState, worldItemSystem);
        if (initializeNewGame) initializeNewGameState();
    }

    private void initializeNewGameState(){
        destructibleObjectSystem.spawnInitialResources(player);

        worldItemSystem.add(new WorldItem(player.getX(), player.getY(), ItemType.STONE_HOE, 1));
        worldItemSystem.add(new WorldItem(player.getX(), player.getY(), ItemType.WHEAT_SEED, 5));
        worldItemSystem.add(new WorldItem(player.getX(), player.getY(), ItemType.SAPLING, 5));
    }

    public static World createForLoad(){
        return new World(false);
    }

    public void update(float delta, PlayerInput playerInput){
        totalPlayTimeSeconds += delta;
        player.update(delta);
        player.move(playerInput.getMoveX(), playerInput.getMoveY(), delta);

        distanceField.update(tileMap.worldToTileX(player.getCenterX()), tileMap.worldToTileY(player.getCenterY()));

        farmingSystem.update(delta);
        handleMatureSaplings();

        for (Enemy e : enemies) e.update(delta);
        projectileSystem.update(delta);

        dayNightCycle.update(delta);
        if (dayNightCycle.justBecameNight()) enemySpawnSystem.startNight(dayNightCycle.getDayCount());
        if (dayNightCycle.isNight()) enemySpawnSystem.update(delta);
        if (dayNightCycle.justBecameDay()){
            enemySpawnSystem.endNight();
            for (Enemy enemy : enemies) enemy.startFleeing(tileMap.getWidth() * Config.TILE_SIZE, tileMap.getHeight() * Config.TILE_SIZE);
        }

        handleHotbarSelection(playerInput);
        handlePrimaryAction(playerInput);
        handleInteract(playerInput);
        handleDropItem(playerInput);
        handleUseItem(playerInput);
        destructibleObjectSystem.processDestroyedObjects();

        worldItemSystem.update(delta);

        for (Enemy enemy : enemies){
            if (!enemy.shouldBeRemoved()) continue;
            if (!enemy.wasKilled()) continue;
            int goldDrop = getRandomGoldDrop();
            if (goldDrop <= 0) continue;
            worldItemSystem.add(new WorldItem(enemy.getX(), enemy.getY(), ItemType.GOLD, goldDrop));
        }
        enemies.removeIf(Enemy::shouldBeRemoved);
    }

    private void handlePrimaryAction(PlayerInput playerInput){
        if (playerInput.isPrimaryActionPressed() && player.canUsePrimaryAction()){

            List<Hittable> targets = new ArrayList<>();
            targets.addAll(enemies);
            targets.addAll(destructibleObjectSystem.getObjects());

            combatSystem.performPrimaryAction(player, targets);
            player.resetPrimaryActionCooldown();
        }
    }

    private int getRandomGoldDrop(){
        int roll = random.nextInt(100);
        if (roll < Config.DOUBLE_GOLD_DROP_CHANCE) return 2;
        if (roll < Config.DOUBLE_GOLD_DROP_CHANCE + Config.SINGLE_GOLD_DROP_CHANCE) return 1;
        return 0;
    }

    private void handleInteract(PlayerInput playerInput){
        if (!playerInput.isInteractPressed()) return;

        if (tryOpenChest()) return;
        if (tryHarvestCrop()) return;
        worldItemSystem.tryPickUpNearest(player);
    }

    private boolean tryOpenChest(){
        Chest nearestChest = destructibleObjectSystem.findNearestChest(player.getX(), player.getY(), Config.PLAYER_PICKUP_RANGE);
        if (nearestChest == null) return false;
        activeChest = nearestChest;
        return true;
    }

    private boolean tryHarvestCrop() {
        Crop nearestCrop = null;
        int nearestTileX = -1;
        int nearestTileY = -1;
        Double nearestDistanceSquared = null;

        for (int x=0; x < tileMap.getWidth(); x++){
            for (int y=0; y < tileMap.getHeight(); y++){
                if (!(farmingSystem.getPlant(x, y) instanceof Crop crop)) continue;

                if (crop.getGrowthStage() != GrowthStage.MATURE) continue;

                float cropWorldX = tileMap.tileToWorldX(x);
                float cropWorldY = tileMap.tileToWorldY(y);

                float dimX = cropWorldX - player.getX();
                float dimY = cropWorldY - player.getY();
                double dist = dimX * dimX + dimY * dimY;
                if (dist > Config.PLAYER_PICKUP_RANGE * Config.PLAYER_PICKUP_RANGE) continue;

                if (nearestDistanceSquared == null || dist < nearestDistanceSquared){
                    nearestDistanceSquared = dist;
                    nearestCrop = crop;
                    nearestTileX = x;
                    nearestTileY = y;
                }
            }
        }
        if (nearestCrop == null) return false;

        float tileWorldX = tileMap.tileToWorldX(nearestTileX);
        float tileWorldY = tileMap.tileToWorldY(nearestTileY);
        float wheatDropX = tileWorldX + Config.TILE_SIZE * 0.20f;
        float seedDropX = tileWorldX + Config.TILE_SIZE * 0.60f;
        float dropY = tileWorldY + Config.TILE_SIZE * 0.20f;
        worldItemSystem.add(new WorldItem(wheatDropX, dropY, ItemType.WHEAT, 1));

        int seedAmount = random.nextInt(100) < 75 ? 1 : 2;
        worldItemSystem.add(new WorldItem(seedDropX , dropY, ItemType.WHEAT_SEED, seedAmount));

        farmingSystem.removePlant(nearestTileX, nearestTileY);
        return true;
    }

    private void handleHotbarSelection(PlayerInput playerInput){
        int selectedSlot = playerInput.getSelectedHotbarSlot();
        if (selectedSlot == -1) return;
        if (selectedSlot == player.getSelectedHotbarSlot()) return;
        player.setSelectedHotbarSlot(selectedSlot);
        cancelTargeting();
    }

    private void handleDropItem(PlayerInput playerInput){
        if (!playerInput.isDropItemPressed()) return;
        ItemType itemType = player.getInventory().getItemTypeBySlot(player.getSelectedHotbarSlot());
        if (itemType == null) return;
        int quantity = 1;

        if (playerInput.isDropWholeStack()){
            quantity = player.getInventory().getQuantityBySlot(player.getSelectedHotbarSlot());
        }

        ItemStack itemStack = player.getInventory().extractFromSlot(player.getSelectedHotbarSlot(), quantity);

        if (itemStack == null) return;
        worldItemSystem.add(new WorldItem(
                                   player.getX() + (random.nextFloat(3) - 0.5f) * Config.TILE_SIZE,
                                   player.getY() + (random.nextFloat(3) - 0.5f) * Config.TILE_SIZE,
                                      itemStack));
    }

    private void handleUseItem(PlayerInput playerInput){
        if (!playerInput.isUseItemPressed()) return;

        int selectedSlot = player.getSelectedHotbarSlot();
        ItemType itemType = player.getInventory().getItemTypeBySlot(selectedSlot);

        if (itemType == null) return;

        if (itemType.getTargetMode() != TargetMode.NONE){
            if (targetMode == itemType.getTargetMode()) {
                targetMode = TargetMode.NONE;
            }
            else {
                targetMode = itemType.getTargetMode();
            }
            return;
        }

        if (targetMode != TargetMode.NONE){
            targetMode = TargetMode.NONE;
        }

        if (itemType == ItemType.HEART_REPAIR){
            player.useHeartRepair();
            player.getInventory().removeFromSlot(selectedSlot, 1);
        }

        if (itemType == ItemType.BREAD){
            player.heal(Config.BREAD_HEALING);
            player.getInventory().removeFromSlot(selectedSlot, 1);
        }
    }

    public boolean isCurrentTargetValid(int tileX, int tileY, float worldX, float worldY){
        if (tileX < 0 || tileY < 0 || tileX >= tileMap.getWidth() || tileY >= tileMap.getHeight()) return false;

        int playerTileX = tileMap.worldToTileX(player.getCenterX());
        int playerTileY = tileMap.worldToTileY(player.getCenterY());

        int distanceX = tileX - playerTileX;
        int distanceY = tileY - playerTileY;

        int distanceSquared = distanceX * distanceX + distanceY * distanceY;
        if (distanceSquared > Config.PLAYER_TARGET_RANGE * Config.PLAYER_TARGET_RANGE) return false;

        targetBounds.set(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE);

        if (targetMode == TargetMode.PLACE){
            if (targetBounds.overlaps(player.getCollisionBounds())) return false;
        }

        if (targetMode == TargetMode.PLANT){
            if (farmingSystem.getPlant(tileX, tileY) != null) return false;
            ItemType selectedItem = player.getInventory().getItemTypeBySlot(player.getSelectedHotbarSlot());
            boolean validPlantTarget = switch (selectedItem){
                case WHEAT_SEED -> farmingSystem.isTilled(tileX, tileY);
                case SAPLING ->
                    tileMap.isTreePlantable(tileX, tileY) && !farmingSystem.isTilled(tileX, tileY);
                default -> false;
            };

            if (!validPlantTarget) return false;
        }

        if (targetMode == TargetMode.TILL){
            if (!tileMap.isTillable(worldX, worldY)) return false;
        }

        if (tileMap.isBlocked(tileX, tileY)) return false;

        for (Enemy enemy : enemies) {
            if (targetBounds.overlaps(enemy.getCollisionBounds())) return false;
        }

        return !destructibleObjectSystem.overlapsAny(targetBounds);
    }

    @Override
    public CraftingResult tryCraft(String recipeId) {
        Recipe recipe = craftingManager.getRecipeById(recipeId);
        if (recipe == null) throw new IllegalArgumentException("Recipe " + recipeId + " does not exist.");
        CraftingResult result = craftingManager.craft(recipe, player.getInventory());

        if (result.isSuccess() && result.getOverflowAmount() > 0){
            worldItemSystem.add(new WorldItem(player.getX(), player.getY(), recipe.getResultType(), result.getOverflowAmount()));
        }

        return result;
    }

    public List<Recipe> getAvailableRecipes(){
        List<Recipe> newList = new ArrayList<>();
        for (Recipe recipe : getRecipes()){
            if (craftingManager.isCategoryUnlocked(recipe.getCategory())) newList.add(recipe);
        }
        return newList;
    }

    @Override
    public CraftStatus getCraftStatus(String recipeId) {
        Recipe recipe = craftingManager.getRecipeById(recipeId);

        if (recipe == null) throw new IllegalArgumentException("Recipe " + recipeId + " does not exist.");
        return craftingManager.getCraftStatus(recipe, player.getInventory());
    }

    public int getOwnedQuantity(ItemType itemType){
        return player.getInventory().getQuantity(itemType);
    }

    public void setCheckpoint(float x, float y){
        checkpointX = x;
        checkpointY = y;
    }

    public void respawnPlayer(){
        player.addBrokenHeart();
        player.setPosition(checkpointX, checkpointY);
        player.restoreHealth();
    }

    public void handleTargetAction(int tileX, int tileY, float worldX, float worldY){
        if (targetMode == TargetMode.NONE) return;
        if (!isCurrentTargetValid(tileX, tileY, worldX, worldY)) return;
        if (targetMode == TargetMode.PLACE) placeTarget(tileX, tileY, worldX, worldY);
        else if (targetMode == TargetMode.PLANT) plantTarget(tileX, tileY, worldX, worldY);
        else if (targetMode == TargetMode.TILL) tillTarget(tileX, tileY, worldX, worldY);
    }

    private void placeTarget(int tileX, int tileY, float worldX, float worldY){
        int selectedSlot = player.getSelectedHotbarSlot();
        ItemType itemType = player.getInventory().getItemTypeBySlot(selectedSlot);
        if (itemType == null) return;

        int removed = player.getInventory().removeFromSlot(selectedSlot, 1);
        if (removed == 0) return;

        destructibleObjectSystem.createAndAdd(worldX, worldY, itemType.getPlacedObjectType());
        if (player.getInventory().getItemTypeBySlot(selectedSlot) == null) cancelTargeting();
    }

    private void plantTarget(int tileX, int tileY, float worldX, float worldY){
        int selectedSlot = player.getSelectedHotbarSlot();
        ItemType itemType = player.getInventory().getItemTypeBySlot(selectedSlot);
        if (itemType == null) return;

        int removed = player.getInventory().removeFromSlot(selectedSlot, 1);
        if (removed == 0) return;

        if (itemType == ItemType.WHEAT_SEED) farmingSystem.plant(CropType.WHEAT, tileX, tileY);
        else if (itemType == ItemType.SAPLING) farmingSystem.plantSapling(tileX, tileY);

        if (player.getInventory().getItemTypeBySlot(selectedSlot) == null) cancelTargeting();
    }

    private void tillTarget(int tileX, int tileY, float worldX, float worldY){
        farmingSystem.till(tileX, tileY);
        player.getInventory().getSlot(player.getSelectedHotbarSlot()).reduceDurability(1);
    }

    private void handleMatureSaplings(){
        for (int x=0; x < tileMap.getWidth(); x++){
            for (int y=0; y < tileMap.getHeight(); y++){
                GrowablePlant plant = farmingSystem.getPlant(x, y);
                if (!(plant instanceof Sapling)) continue;
                if (plant.getGrowthStage() != GrowthStage.MATURE) continue;
                if (!isTreeSpawnPositionValid(x, y)) continue; // neko stoji na tileu, sačekaj

                farmingSystem.removePlant(x, y);
                destructibleObjectSystem.createAndAdd(tileMap.tileToWorldX(x), tileMap.tileToWorldY(y), DestructibleObjectType.TREE);
            }
        }
    }

    private boolean isTreeSpawnPositionValid(int tileX, int tileY){
        float worldX = tileMap.tileToWorldX(tileX);
        float worldY = tileMap.tileToWorldY(tileY);
        Rectangle treeBounds = new Rectangle(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE);

        if (treeBounds.overlaps(player.getCollisionBounds())) return false;

        for (Enemy enemy : enemies) {
            if (treeBounds.overlaps(enemy.getCollisionBounds())) return false;
        }

        return true;
    }

    public void applyPersistentState(float checkpointX, float checkpointY, double playTimeSeconds){
        if (playTimeSeconds < 0) throw new IllegalArgumentException("Play time cannot be negative.");

        this.checkpointX = checkpointX;
        this.checkpointY = checkpointY;
        this.totalPlayTimeSeconds = playTimeSeconds;
    }

    public void setTargetMode(TargetMode targetMode){this.targetMode = targetMode;}
    public void cancelTargeting(){targetMode = TargetMode.NONE;}
    public void closeChest(){activeChest = null;}

    public List<Recipe> getRecipes(){
        return craftingManager.getRecipes();
    }
    public TileMap getTileMap(){return tileMap;}
    public Player getPlayer(){return player;}
    public List<Enemy> getEnemies() { return enemies; }
    public DayNightCycle getDayNightCycle() {return dayNightCycle;}
    public TargetMode getTargetMode() {return targetMode;}
    public Chest getActiveChest() {return activeChest;}
    public FarmingSystem getFarmingSystem() {return farmingSystem;}
    public ProjectileSystem getProjectileSystem() {return projectileSystem;}
    public double getTotalPlayTimeSeconds() {return totalPlayTimeSeconds;}
    public ProgressionState getProgressionState() {return progressionState;}
    public EnemySpawnSystem getEnemySpawnSystem() {return enemySpawnSystem;}
    public float getCheckpointX() {return checkpointX;}
    public float getCheckpointY() {return checkpointY;}
    public List<WorldItem> getGroundItems(){return worldItemSystem.getItems();}
    public List<DestructibleObject> getDestructibleObjects(){return destructibleObjectSystem.getObjects();}
    public DestructibleObjectSystem getDestructibleObjectSystem(){return destructibleObjectSystem;}

    public void dispose(){
        tileMap.dispose();
    }
}
