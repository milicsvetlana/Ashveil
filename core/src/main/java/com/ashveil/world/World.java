package com.ashveil.world;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.combat.CombatSystem;
import com.ashveil.combat.Hittable;
import com.ashveil.entities.enemies.Enemy;
import com.ashveil.entities.enemies.EnemyType;
import com.ashveil.entities.Player;
import com.ashveil.entities.enemies.Shade;
import com.ashveil.items.crafting.CraftStatus;
import com.ashveil.items.crafting.CraftingManager;
import com.ashveil.items.crafting.CraftingResult;
import com.ashveil.items.crafting.Recipe;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.objects.ResourceObject;
import com.ashveil.objects.ResourceType;
import com.ashveil.input.PlayerInput;
import com.ashveil.progression.ProgressionState;
import com.ashveil.items.crafting.CraftingAccess;
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
    private final List<WorldItem> groundItems;
    private final List<ResourceObject> resourceObjects;

    private final CollisionSystem collisionSystem;

    private final Player player;
    private final List<Enemy> enemies;

    private float checkpointX;
    private float checkpointY;

    private final ProgressionState progressionState;

    public World(){
        tileMap = new TileMap();
        collisionSystem = new CollisionSystem(tileMap);
        player = new Player(tileMap.getPlayerSpawnX(), tileMap.getPlayerSpawnY(), tileMap, collisionSystem);
        checkpointX = tileMap.getPlayerSpawnX();
        checkpointY = tileMap.getPlayerSpawnY();
        enemies = new ArrayList<>();
        resourceObjects = new ArrayList<>();
        groundItems = new ArrayList<>();
        craftingManager = new CraftingManager();
        combatSystem = new CombatSystem();
        progressionState = new ProgressionState();
        spawnInitialResources();
        dayNightCycle = new DayNightCycle();
    }

    public void update(float delta, PlayerInput playerInput){
        player.update(delta);

        for (Enemy e : enemies){
            e.update(delta);
        }

        player.move(playerInput.getMoveX(), playerInput.getMoveY(), delta);

        if (dayNightCycle.justBecameNight()){
            spawnEnemies();
        }

        handleHotbarSelection(playerInput);
        handlePrimaryAction(playerInput);
        handlePickup(playerInput);
        handleDropItem(playerInput);
        dayNightCycle.update(delta);

        for (WorldItem item : groundItems){
            item.update(delta);
        }

        groundItems.removeIf(WorldItem::shouldDespawn);
        for (Enemy enemy : enemies){
            if (!enemy.shouldBeRemoved()) continue;
            addGroundItem(new WorldItem(enemy.getX(), enemy.getY(), ItemType.GOLD, getRandomGoldDrop()));
        }
        enemies.removeIf(Enemy::shouldBeRemoved);
    }

    private void handlePrimaryAction(PlayerInput playerInput){
        if (playerInput.isPrimaryActionPressed() && player.canUsePrimaryAction()){

            List<Hittable> targets = new ArrayList<>();
            targets.addAll(enemies);
            targets.addAll(resourceObjects);

            int numberOfHits = combatSystem.performPrimaryAction(player, targets);
            player.resetPrimaryActionCooldown();

            for (ResourceObject o : resourceObjects){
                if (o.isDestroyed()){
                    int dropAmount = getResourceDropAmount(o);

                    addGroundItem(new WorldItem(o.getX() + (random.nextInt(3) - 1) * Config.TILE_SIZE,
                                                o.getY() + (random.nextInt(3) - 1) * Config.TILE_SIZE,
                                                   o.getType().getDrop(), dropAmount
                    ));

                    collisionSystem.unregister(o);
                }
            }

            resourceObjects.removeIf(ResourceObject::isDestroyed);
        }
    }

    private int getResourceDropAmount(ResourceObject resource){
        if (resource.getType() == ResourceType.TREE && !progressionState.isFirstTreeDropClaimed()) {
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

    private void handlePickup(PlayerInput playerInput) {
        if (!playerInput.isInteractPressed()) return;

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
        if(nearestItem == null) return;
        if (nearestItem.getType() == ItemType.GOLD){
            player.getWallet().addGold(nearestItem.getAmount());
            groundItems.remove(nearestItem);
            return;
        }
        int remaining = player.pickUp(nearestItem.getType(), nearestItem.getAmount());
        if (remaining == 0) groundItems.remove(nearestItem);
        else nearestItem.setAmount(remaining);
    }

    private void handleHotbarSelection(PlayerInput playerInput){
        int selectedSlot = playerInput.getSelectedHotbarSlot();
        if (selectedSlot == -1) return;
        player.setSelectedHotbarSlot(selectedSlot);
    }

    private void handleDropItem(PlayerInput playerInput){
        if (!playerInput.isDropItemPressed()) return;
        ItemType itemType = player.getInventory().getItemTypeBySlot(player.getSelectedHotbarSlot());
        if (itemType == null) return;
        int quantity = 1;

        if (playerInput.isDropWholeStack()){
            quantity = player.getInventory().getQuantityBySlot(player.getSelectedHotbarSlot());
        }

        int removed = player.getInventory().removeFromSlot(player.getSelectedHotbarSlot(), quantity);
        if (removed == 0) return;
        addGroundItem(new WorldItem(
            player.getX() + (random.nextFloat(3) - 0.5f) * Config.TILE_SIZE,
            player.getY() + (random.nextFloat(3) - 0.5f) * Config.TILE_SIZE,
            itemType, removed));
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

    private void spawnInitialResources(){
        int type;
        int numberOfItems = random.nextInt(Config.MAX_EXTRA_INITIAL_RESOURCES + 1);

        for (ResourceType resourceType : ResourceType.values()){
            for (int i=0; i < Config.MIN_INITIAL_RESOURCES; i++){
                spawnResource(resourceType);
            }
        }

        for (int i=0; i < numberOfItems; i++) {
            type = random.nextInt(ResourceType.values().length);
            spawnResource(ResourceType.values()[type]);
        }
    }

    private void spawnResource(ResourceType resourceType){
        int tileX, tileY;
        do {
            tileX = random.nextInt(tileMap.getWidth());
            tileY = random.nextInt(tileMap.getHeight());
        } while (!isResourcePositionValid(tileX, tileY));

        ResourceObject object = new ResourceObject(tileX * Config.TILE_SIZE, tileY * Config.TILE_SIZE, resourceType);

        resourceObjects.add(object);
        collisionSystem.register(object);
    }

    private boolean isResourcePositionValid(int tileX, int tileY){
        if (tileMap.isBlocked(tileX, tileY)) return false;

        float worldX = tileX * Config.TILE_SIZE;
        float worldY = tileY * Config.TILE_SIZE;

        for (ResourceObject resource : resourceObjects) {
            if (resource.getX() == worldX && resource.getY() == worldY) return false;
        }

        float playerTileX = (int) (player.getX() / Config.TILE_SIZE);
        float playerTileY = (int) (player.getY() / Config.TILE_SIZE);

        int distanceFromPlayerX = (int) Math.abs(tileX - playerTileX);
        int distanceFromPlayerY = (int) Math.abs(tileY - playerTileY);

        if (distanceFromPlayerX <= Config.INITIAL_SPAWN_CLEAR_RADIUS
            && distanceFromPlayerY <= Config.INITIAL_SPAWN_CLEAR_RADIUS) return false;

        return true;
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
        groundItems.add(new WorldItem(newItem.getX(), newItem.getY(), newItem.getType(), remaining));
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

    public List<Recipe> getRecipes(){
        return craftingManager.getRecipes();
    }
    public TileMap getTileMap(){return tileMap;}
    public Player getPlayer(){return player;}
    public List<Enemy> getEnemies() { return enemies; }
    public List<WorldItem> getGroundItems() {return groundItems;}
    public List<ResourceObject> getResourceObjects() {return resourceObjects;}
    public DayNightCycle getDayNightCycle() {return dayNightCycle;}

    public void dispose(){
        tileMap.dispose();
    }
}
