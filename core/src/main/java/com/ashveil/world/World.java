package com.ashveil.world;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.combat.CombatSystem;
import com.ashveil.combat.Hittable;
import com.ashveil.entities.enemies.Enemy;
import com.ashveil.entities.enemies.EnemyType;
import com.ashveil.entities.Player;
import com.ashveil.entities.enemies.Shade;
import com.ashveil.farming.Crop;
import com.ashveil.farming.CropStage;
import com.ashveil.farming.CropType;
import com.ashveil.farming.FarmingSystem;
import com.ashveil.items.crafting.CraftStatus;
import com.ashveil.items.crafting.CraftingManager;
import com.ashveil.items.crafting.CraftingResult;
import com.ashveil.items.crafting.Recipe;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.objects.Chest;
import com.ashveil.objects.DestructibleObject;
import com.ashveil.objects.DestructibleObjectType;
import com.ashveil.input.PlayerInput;
import com.ashveil.progression.ProgressionState;
import com.ashveil.items.crafting.CraftingAccess;
import com.ashveil.targeting.TargetMode;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class World implements CraftingAccess {

    private final Random random = new Random();

    private final TileMap tileMap;
    private final DayNightCycle dayNightCycle;

    private final CraftingManager craftingManager;
    private final CombatSystem combatSystem;
    private final List<WorldItem> groundItems;
    private final List<DestructibleObject> destructibleObjects;

    private final CollisionSystem collisionSystem;

    private final Player player;
    private final List<Enemy> enemies;

    private float checkpointX;
    private float checkpointY;

    private final ProgressionState progressionState;
    private TargetMode targetMode;
    private Chest activeChest;
    private Rectangle targetBounds;
    private final Map<DestructibleObjectType, ItemType> destructibleObjectDrops;
    private final FarmingSystem farmingSystem;

    public World(){
        tileMap = new TileMap();
        collisionSystem = new CollisionSystem(tileMap);
        player = new Player(tileMap.getPlayerSpawnX(), tileMap.getPlayerSpawnY(), tileMap, collisionSystem);
        checkpointX = tileMap.getPlayerSpawnX();
        checkpointY = tileMap.getPlayerSpawnY();
        enemies = new ArrayList<>();
        destructibleObjects = new ArrayList<>();
        groundItems = new ArrayList<>();
        craftingManager = new CraftingManager();
        combatSystem = new CombatSystem();
        progressionState = new ProgressionState();
        spawnInitialResources();
        dayNightCycle = new DayNightCycle();
        targetMode = TargetMode.NONE;
        targetBounds = new Rectangle();
        activeChest = null;
        farmingSystem = new FarmingSystem(tileMap.getWidth(), tileMap.getHeight());

        destructibleObjectDrops = Map.of(
            DestructibleObjectType.TREE, ItemType.WOOD,
            DestructibleObjectType.ROCK, ItemType.STONE,
            DestructibleObjectType.FENCE, ItemType.FENCE,
            DestructibleObjectType.CHEST, ItemType.CHEST
        );

        addGroundItem(new WorldItem(player.getX(), player.getY(), ItemType.WHEAT_SEED, 4));
        addGroundItem(new WorldItem(player.getX(), player.getY(), ItemType.STONE_HOE, 1));
    }

    public void update(float delta, PlayerInput playerInput){
        player.update(delta);
        farmingSystem.update(delta);

        for (Enemy e : enemies){
            e.update(delta);
        }

        player.move(playerInput.getMoveX(), playerInput.getMoveY(), delta);

        if (dayNightCycle.justBecameNight()){
            spawnEnemies();
        }

        handleHotbarSelection(playerInput);
        handlePrimaryAction(playerInput);
        handleInteract(playerInput);
        handleDropItem(playerInput);
        handleUseItem(playerInput);
        dayNightCycle.update(delta);

        for (WorldItem item : groundItems){
            item.update(delta);
        }

        groundItems.removeIf(WorldItem::shouldDespawn);
        for (Enemy enemy : enemies){
            if (!enemy.shouldBeRemoved()) continue;
            int goldDrop = getRandomGoldDrop();
            if (goldDrop <= 0) continue;
            addGroundItem(new WorldItem(enemy.getX(), enemy.getY(), ItemType.GOLD, goldDrop));
        }
        enemies.removeIf(Enemy::shouldBeRemoved);
    }

    private void handlePrimaryAction(PlayerInput playerInput){
        if (playerInput.isPrimaryActionPressed() && player.canUsePrimaryAction()){

            List<Hittable> targets = new ArrayList<>();
            targets.addAll(enemies);
            targets.addAll(destructibleObjects);

            int numberOfHits = combatSystem.performPrimaryAction(player, targets);
            player.resetPrimaryActionCooldown();

            for (DestructibleObject o : destructibleObjects){
                if (o.isDestroyed()){
                    int dropAmount = getDropAmount(o);

                    addGroundItem(new WorldItem(o.getX() + (random.nextInt(3) - 1) * Config.TILE_SIZE,
                                                o.getY() + (random.nextInt(3) - 1) * Config.TILE_SIZE,
                                                   destructibleObjectDrops.get(o.getType()), dropAmount
                    ));

                    if (o.getType() == DestructibleObjectType.CHEST){
                       Chest chest = (Chest) o;
                       for (int i=0; i < chest.getChestInventory().getSize(); i++){
                           ItemStack itemStack = chest.getChestInventory().getSlot(i);
                           if (itemStack == null) continue;

                           addGroundItem(new WorldItem(o.getX() + (random.nextInt(3) - 1) * Config.TILE_SIZE,
                                                       o.getY() + (random.nextInt(3) - 1) * Config.TILE_SIZE,
                                                          itemStack));
                       }
                    }

                    collisionSystem.unregister(o);
                }
            }

            destructibleObjects.removeIf(DestructibleObject::isDestroyed);
        }
    }

    private int getDropAmount(DestructibleObject resource){
        if (resource.getType() == DestructibleObjectType.TREE && !progressionState.isFirstTreeDropClaimed()) {
            progressionState.claimFirstTreeDrop();
            return Config.FIRST_TREE_DROP_AMOUNT;
        }

        return random.nextInt(resource.getType().getMaxDrop() - resource.getType().getMinDrop() + 1)
                                + resource.getType().getMinDrop();
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
        tryPickUpGroundItem();
    }

    private boolean tryOpenChest(){
        Chest nearestChest = null;
        Double nearestDistanceSquared = null;
        for (DestructibleObject object : destructibleObjects){
            if (object.getType() != DestructibleObjectType.CHEST) continue;

            float dimX = object.getX() - player.getX();
            float dimY = object.getY() - player.getY();
            double dist = dimX * dimX + dimY * dimY;
            if (dist > Config.PLAYER_PICKUP_RANGE * Config.PLAYER_PICKUP_RANGE) continue;
            if (nearestDistanceSquared == null || dist < nearestDistanceSquared){
                nearestDistanceSquared = dist;
                nearestChest = (Chest) object;
            }
        }
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
                Crop crop = farmingSystem.getCrop(x, y);

                if (crop == null) continue;
                if (crop.getCropStage() != CropStage.MATURE) continue;

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
        addGroundItem(new WorldItem(wheatDropX, dropY, ItemType.WHEAT, 1));

        int seedAmount = random.nextInt(100) < 75 ? 1 : 2;
        addGroundItem(new WorldItem(seedDropX , dropY, ItemType.WHEAT_SEED, seedAmount));

        farmingSystem.removeCrop(nearestTileX, nearestTileY);
        return true;
    }

    private boolean tryPickUpGroundItem() {
        WorldItem nearestItem = getWorldItem();
        if(nearestItem == null) return false;
        if (nearestItem.getType() == ItemType.GOLD){
            player.getWallet().addGold(nearestItem.getAmount());
            groundItems.remove(nearestItem);
            return true;
        }
        int remaining = player.getInventory().addStack(nearestItem.getStack());
        if (remaining == 0) groundItems.remove(nearestItem);
        return true;
    }

    private WorldItem getWorldItem() {
        WorldItem nearestItem = null;
        Double nearestDistanceSquared = null;
        for (WorldItem item : groundItems){
            float dimX = item.getX() - player.getX();
            float dimY = item.getY() - player.getY();
            double dist = dimX * dimX + dimY * dimY;
            if (dist > Config.PLAYER_PICKUP_RANGE * Config.PLAYER_PICKUP_RANGE) continue;

            if (nearestDistanceSquared == null || dist < nearestDistanceSquared){
                nearestDistanceSquared = dist;
                nearestItem = item;
            }
        }
        return nearestItem;
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
        addGroundItem(new WorldItem(
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
    }

    private void spawnEnemies(){
        for (int i = 0; i < dayNightCycle.getDayCount() * 2; i++) {
            spawnEnemy(EnemyType.SHADE);
        }
    }

    private void spawnEnemy(EnemyType enemyType){
        int tileX;
        int tileY;

        do {
            tileX = random.nextInt(tileMap.getWidth());
            tileY = random.nextInt(tileMap.getHeight());
        } while (!isEnemySpawnPositionValid(tileX, tileY, enemyType));

        float worldX = tileX * Config.TILE_SIZE;
        float worldY = tileY * Config.TILE_SIZE;

        if (enemyType == EnemyType.SHADE) {
            enemies.add(new Shade(worldX, worldY, player, collisionSystem));
        }
    }

    private boolean isEnemySpawnPositionValid(int tileX, int tileY, EnemyType enemyType){
        float worldX = tileX * Config.TILE_SIZE;
        float worldY = tileY * Config.TILE_SIZE;

        if (collisionSystem.isBlocked(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE, enemyType.getMovementType())) return false;

        Rectangle spawnBounds = new Rectangle(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE);

        if (spawnBounds.overlaps(player.getCollisionBounds())) return false;

        for (Enemy enemy : enemies) {
            if (spawnBounds.overlaps(enemy.getCollisionBounds())) return false;
        }

        return true;
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
            if(!farmingSystem.isTilled(tileX, tileY) && farmingSystem.getCrop(tileX, tileY) == null) return false;
        }

        if (targetMode == TargetMode.TILL){
            if (!tileMap.isTillable(worldX, worldY) || farmingSystem.isTilled(tileX, tileY)) return false;
        }

        if (tileMap.isBlocked(tileX, tileY)) return false;

        for (Enemy enemy : enemies) {
            if (targetBounds.overlaps(enemy.getCollisionBounds())) return false;
        }

        for (DestructibleObject destructibleObject : destructibleObjects){
            if (targetBounds.overlaps(destructibleObject.getCollisionBounds())) return false;
         }

        return true;
    }

    private void spawnInitialResources(){
        List<DestructibleObjectType> naturalTypes = new ArrayList<>();

        for (DestructibleObjectType type : DestructibleObjectType.values()) {
            if (type.spawnsNaturally()) naturalTypes.add(type);
        }

        for (DestructibleObjectType type : naturalTypes) {
            for (int i = 0; i < Config.MIN_INITIAL_RESOURCES; i++) spawnNaturalObject(type);
        }

        int numberOfExtraResources = random.nextInt(Config.MAX_EXTRA_INITIAL_RESOURCES + 1);

        for (int i = 0; i < numberOfExtraResources; i++) {
            DestructibleObjectType randomType = naturalTypes.get(random.nextInt(naturalTypes.size()));
            spawnNaturalObject(randomType);
        }
    }

    private void spawnNaturalObject(DestructibleObjectType destructibleObjectType){
        int tileX, tileY;
        do {
            tileX = random.nextInt(tileMap.getWidth());
            tileY = random.nextInt(tileMap.getHeight());
        } while (!isResourcePositionValid(tileX, tileY));

        DestructibleObject object = createDestructibleObject(tileX * Config.TILE_SIZE, tileY * Config.TILE_SIZE, destructibleObjectType);

        destructibleObjects.add(object);
        collisionSystem.register(object);
    }

    private DestructibleObject createDestructibleObject(float worldX, float worldY, DestructibleObjectType type){
        if (type == DestructibleObjectType.CHEST) return new Chest(worldX, worldY);
        return new DestructibleObject(worldX, worldY, type);
    }

    private boolean isResourcePositionValid(int tileX, int tileY){
        if (tileMap.isBlocked(tileX, tileY)) return false;

        float worldX = tileX * Config.TILE_SIZE;
        float worldY = tileY * Config.TILE_SIZE;

        for (DestructibleObject resource : destructibleObjects) {
            if (resource.getX() == worldX && resource.getY() == worldY) return false;
        }

        float playerTileX = (int) (player.getX() / Config.TILE_SIZE);
        float playerTileY = (int) (player.getY() / Config.TILE_SIZE);

        int distanceFromPlayerX = (int) Math.abs(tileX - playerTileX);
        int distanceFromPlayerY = (int) Math.abs(tileY - playerTileY);

        return distanceFromPlayerX > Config.INITIAL_SPAWN_CLEAR_RADIUS
            || distanceFromPlayerY > Config.INITIAL_SPAWN_CLEAR_RADIUS;
    }

    private void addGroundItem(WorldItem newItem){
        if (newItem.getAmount() <= 0) return;
        int remaining = newItem.getAmount();

        if (newItem.getType().isStackable()){
            for (WorldItem item : groundItems){
                if (newItem.getType() != item.getType()) continue;

                float dimX = item.getX() - newItem.getX();
                float dimY = item.getY() - newItem.getY();
                double dist = Math.sqrt(dimX * dimX + dimY * dimY);

                if (dist > Config.WORLD_ITEM_MERGE_RANGE) continue;

                int previousRemaining = remaining;
                remaining = item.addAmount(remaining);

                if (remaining < previousRemaining) item.resetLifetime();

                if (remaining == 0) return;
            }

        }
        if (remaining != newItem.getAmount()) {
            newItem.setAmount(remaining);
        }
        groundItems.add(newItem);
        checkSafetyLimit();
    }

    private void checkSafetyLimit(){
        if (groundItems.size() <= Config.WORLD_MAX_NUMBER_OF_ITEMS) return;

        for (int i=0; i < groundItems.size(); i++){
            WorldItem item = groundItems.get(i);

            if (item.getType().despawnsOnGround()){
                groundItems.remove(i);
                return;
            }
        }
    }

    @Override
    public CraftingResult tryCraft(String recipeId) {
        Recipe recipe = craftingManager.getRecipeById(recipeId);
        if (recipe == null) throw new IllegalArgumentException("Recipe " + recipeId + " does not exist.");
        CraftingResult result = craftingManager.craft(recipe, player.getInventory());

        if (result.isSuccess() && result.getOverflowAmount() > 0){
            addGroundItem(new WorldItem(player.getX(), player.getY(), recipe.getResultType(), result.getOverflowAmount()));
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

        DestructibleObject object = createDestructibleObject(worldX, worldY, itemType.getPlacedObjectType());
        destructibleObjects.add(object);
        collisionSystem.register(object);

        if (player.getInventory().getItemTypeBySlot(selectedSlot) == null) cancelTargeting();
    }

    private void plantTarget(int tileX, int tileY, float worldX, float worldY){
        int selectedSlot = player.getSelectedHotbarSlot();
        ItemType itemType = player.getInventory().getItemTypeBySlot(selectedSlot);

        if (itemType != ItemType.WHEAT_SEED) return;

        int removed = player.getInventory().removeFromSlot(selectedSlot, 1);
        if (removed == 0) return;

        farmingSystem.plant(CropType.WHEAT, tileX, tileY);

        if (player.getInventory().getItemTypeBySlot(selectedSlot) == null) cancelTargeting();
    }

    private void tillTarget(int tileX, int tileY, float worldX, float worldY){
        farmingSystem.till(tileX, tileY);
        player.getInventory().getSlot(player.getSelectedHotbarSlot()).reduceDurability(1);
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
    public List<WorldItem> getGroundItems() {return groundItems;}
    public List<DestructibleObject> getDestructibleObjects() {return destructibleObjects;}
    public DayNightCycle getDayNightCycle() {return dayNightCycle;}
    public TargetMode getTargetMode() {return targetMode;}
    public Chest getActiveChest() {return activeChest;}
    public FarmingSystem getFarmingSystem() {return farmingSystem;}

    public void dispose(){
        tileMap.dispose();
    }
}
