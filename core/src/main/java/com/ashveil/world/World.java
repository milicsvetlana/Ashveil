package com.ashveil.world;

import com.ashveil.Config;
import com.ashveil.combat.CombatSystem;
import com.ashveil.combat.Hittable;
import com.ashveil.combat.ProjectileSystem;
import com.ashveil.entities.enemies.*;
import com.ashveil.entities.Player;
import com.ashveil.farming.*;
import com.ashveil.guidance.GameEvent;
import com.ashveil.guidance.GuidanceSystem;
import com.ashveil.items.crafting.CraftStatus;
import com.ashveil.items.crafting.CraftingManager;
import com.ashveil.items.crafting.CraftingResult;
import com.ashveil.items.crafting.Recipe;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.objects.Chest;
import com.ashveil.objects.DestructibleObject;
import com.ashveil.objects.DestructibleObjectSystem;
import com.ashveil.objects.DestructibleObjectType;
import com.ashveil.input.PlayerInput;
import com.ashveil.progression.ProgressionState;
import com.ashveil.items.crafting.CraftingAccess;
import com.ashveil.targeting.TargetMode;
import com.ashveil.world.area.AreaID;
import com.ashveil.world.area.AreaManager;
import com.ashveil.world.area.AreaRuntime;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.*;

public class World implements CraftingAccess, WorldMapAccess {

    private final Random random = new Random();

    private final AreaManager areaManager;

    private final Player player;

    private final CombatSystem combatSystem;

    private final CraftingManager craftingManager;
    private final ProgressionState progressionState;
    private final GuidanceSystem guidanceSystem;
    private final DayNightCycle dayNightCycle;

    private final Map<AreaID, AreaRuntime> areaRuntimes;
    private AreaRuntime currentAreaRuntime;

    private TargetMode targetMode;
    private Chest activeChest;
    private Rectangle targetBounds;

    private float checkpointX;
    private float checkpointY;

    private double totalPlayTimeSeconds;
    private float boatTravelCooldownRemaining;
    private boolean worldMapRequested;

    public World(){
        areaManager = new AreaManager(AreaID.MAIN_ISLAND);

        progressionState = new ProgressionState();
        guidanceSystem = new GuidanceSystem();
        combatSystem = new CombatSystem();
        craftingManager = new CraftingManager(progressionState);
        dayNightCycle = new DayNightCycle();

        player = new Player(0f, 0f);

        areaRuntimes = new EnumMap<>(AreaID.class);
        AreaRuntime mainIsland = new AreaRuntime(areaManager.getCurrentArea(), player, progressionState);

        areaRuntimes.put(AreaID.MAIN_ISLAND, mainIsland);
        currentAreaRuntime = mainIsland;

        player.setAreaEnvironment(currentAreaRuntime.getTileMap(), currentAreaRuntime.getCollisionSystem());
        updateBoatVisibility();

        Vector2 playerSpawn = currentAreaRuntime.getTileMap().getObjectPosition("Objects", "player_spawn");
        player.setPosition(playerSpawn.x, playerSpawn.y);
        checkpointX = playerSpawn.x;
        checkpointY = playerSpawn.y;

        initializeNewGameState();

        targetMode = TargetMode.NONE;
        targetBounds = new Rectangle();
        activeChest = null;

        totalPlayTimeSeconds = 0;
        boatTravelCooldownRemaining = 0;
        worldMapRequested = false;
    }

    private World(AreaID areaId, float playerX, float playerY){
        areaManager = new AreaManager(areaId);
        progressionState = new ProgressionState();
        guidanceSystem = new GuidanceSystem();
        combatSystem = new CombatSystem();
        craftingManager = new CraftingManager(progressionState);
        dayNightCycle = new DayNightCycle();

        player = new Player(playerX, playerY);

        areaRuntimes = new EnumMap<>(AreaID.class);

        AreaRuntime loadedArea = new AreaRuntime(areaManager.getCurrentArea(), player, progressionState);
        areaRuntimes.put(areaId, loadedArea);
        currentAreaRuntime = loadedArea;

        player.setAreaEnvironment(currentAreaRuntime.getTileMap(), currentAreaRuntime.getCollisionSystem());

        checkpointX = playerX;
        checkpointY = playerY;

        targetMode = TargetMode.NONE;
        targetBounds = new Rectangle();
        activeChest = null;

        totalPlayTimeSeconds = 0;
        boatTravelCooldownRemaining = 0;
        worldMapRequested = false;
    }

    private void initializeNewGameState(){
        currentAreaRuntime.getDestructibleObjectSystem().spawnInitialResources(player);
        spawnStarterChest();
    }

    private void spawnStarterChest(){
        float chestX = player.getX() + Config.TILE_SIZE * 2f;
        float chestY = player.getY();

        Chest chest = (Chest) currentAreaRuntime.getDestructibleObjectSystem().createAndAdd(chestX, chestY, DestructibleObjectType.CHEST);

        chest.getChestInventory().addItem(ItemType.STONE_HOE, 1);
        chest.getChestInventory().addItem(ItemType.WHEAT_SEED, 5);
        chest.getChestInventory().addItem(ItemType.BOAT_KIT, 1);
    }

    public static World createForLoad(AreaID areaID, float playerX, float playerY){
        if (areaID == null) throw new IllegalArgumentException("Area id cannot be null");
        return new World(areaID, playerX, playerY);
    }

    public void update(float delta, PlayerInput playerInput){
        totalPlayTimeSeconds += delta;
        if (boatTravelCooldownRemaining >= 0f){
            boatTravelCooldownRemaining = Math.max(0f, boatTravelCooldownRemaining - delta);
        }
        player.update(delta);
        player.move(playerInput.getMoveX(), playerInput.getMoveY(), delta);

        currentAreaRuntime.getDistanceField().update(getTileMap().worldToTileX(player.getCenterX()), getTileMap().worldToTileY(player.getCenterY()));

        getFarmingSystem().update(delta);
        handleMatureSaplings();

        for (Enemy e : getEnemies()) e.update(delta);
        getProjectileSystem().update(delta);

        dayNightCycle.update(delta);
        if (dayNightCycle.justBecameNight()) startOrdinaryNightForCurrentArea();
        if (dayNightCycle.isNight()) getEnemySpawnSystem().update(delta);
        if (dayNightCycle.justBecameDay()){
            getEnemySpawnSystem().endNight();
            for (Enemy enemy : getEnemies()) enemy.startFleeing(getTileMap().getWidth() * Config.TILE_SIZE, getTileMap().getHeight() * Config.TILE_SIZE);
        }

        handleHotbarSelection(playerInput);
        handlePrimaryAction(playerInput);
        handleInteract(playerInput);
        handleDropItem(playerInput);
        handleUseItem(playerInput);
        getDestructibleObjectSystem().processDestroyedObjects();

        getWorldItemSystem().update(delta);

        for (Enemy enemy : getEnemies()){
            if (!enemy.shouldBeRemoved()) continue;
            if (!enemy.wasKilled()) continue;
            int goldDrop = getRandomGoldDrop();
            if (goldDrop <= 0) continue;
            getWorldItemSystem().add(new WorldItem(enemy.getX(), enemy.getY(), ItemType.GOLD, goldDrop));
        }
        getEnemies().removeIf(Enemy::shouldBeRemoved);
    }

    private void startOrdinaryNightForCurrentArea(){
        int dayCount = dayNightCycle.getDayCount();

        getEnemySpawnSystem().startNight(dayCount);
        currentAreaRuntime.markOrdinaryNightStarted(dayCount);
    }

    private void handlePrimaryAction(PlayerInput playerInput){
        if (playerInput.isPrimaryActionPressed() && player.canUsePrimaryAction()){

            List<Hittable> targets = new ArrayList<>();
            targets.addAll(getEnemies());
            targets.addAll(getDestructibleObjectSystem().getObjects());

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

        if (tryUseBoat()) return;
        if (tryOpenChest()) return;
        if (tryHarvestCrop()) return;
        getWorldItemSystem().tryPickUpNearest(player);
    }

    private boolean tryOpenChest(){
        Chest nearestChest = getDestructibleObjectSystem().findNearestChest(player.getX(), player.getY(), Config.PLAYER_PICKUP_RANGE);
        if (nearestChest == null) return false;
        activeChest = nearestChest;
        return true;
    }

    private boolean tryHarvestCrop() {
        Crop nearestCrop = null;
        int nearestTileX = -1;
        int nearestTileY = -1;
        Double nearestDistanceSquared = null;

        for (int x=0; x < getTileMap().getWidth(); x++){
            for (int y=0; y < getTileMap().getHeight(); y++){
                if (!(getFarmingSystem().getPlant(x, y) instanceof Crop crop)) continue;

                if (crop.getGrowthStage() != GrowthStage.MATURE) continue;

                float cropWorldX = getTileMap().tileToWorldX(x);
                float cropWorldY = getTileMap().tileToWorldY(y);

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

        float tileWorldX = getTileMap().tileToWorldX(nearestTileX);
        float tileWorldY = getTileMap().tileToWorldY(nearestTileY);
        float wheatDropX = tileWorldX + Config.TILE_SIZE * 0.20f;
        float seedDropX = tileWorldX + Config.TILE_SIZE * 0.60f;
        float dropY = tileWorldY + Config.TILE_SIZE * 0.20f;
        getWorldItemSystem().add(new WorldItem(wheatDropX, dropY, ItemType.WHEAT, 1));

        int seedAmount = random.nextInt(100) < 75 ? 1 : 2;
        getWorldItemSystem().add(new WorldItem(seedDropX , dropY, ItemType.WHEAT_SEED, seedAmount));

        getFarmingSystem().removePlant(nearestTileX, nearestTileY);
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
        getWorldItemSystem().add(new WorldItem(
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
        if (tileX < 0 || tileY < 0 || tileX >= getTileMap().getWidth() || tileY >= getTileMap().getHeight()) return false;

        int playerTileX = getTileMap().worldToTileX(player.getCenterX());
        int playerTileY = getTileMap().worldToTileY(player.getCenterY());

        int distanceX = tileX - playerTileX;
        int distanceY = tileY - playerTileY;

        int distanceSquared = distanceX * distanceX + distanceY * distanceY;
        if (distanceSquared > Config.PLAYER_TARGET_RANGE * Config.PLAYER_TARGET_RANGE) return false;

        targetBounds.set(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE);

        if (targetMode == TargetMode.PLACE){
            if (targetBounds.overlaps(player.getCollisionBounds())) return false;
        }

        if (targetMode == TargetMode.PLANT){
            if (getFarmingSystem().getPlant(tileX, tileY) != null) return false;
            ItemType selectedItem = player.getInventory().getItemTypeBySlot(player.getSelectedHotbarSlot());
            boolean validPlantTarget = switch (selectedItem){
                case WHEAT_SEED -> getFarmingSystem().isTilled(tileX, tileY);
                case SAPLING ->
                    getTileMap().isTreePlantable(tileX, tileY) && !getFarmingSystem().isTilled(tileX, tileY);
                default -> false;
            };

            if (!validPlantTarget) return false;
        }

        if (targetMode == TargetMode.TILL){
            if (!getTileMap().isTillable(worldX, worldY)) return false;
        }

        if (getTileMap().isBlocked(tileX, tileY)) return false;

        for (Enemy enemy : getEnemies()) {
            if (targetBounds.overlaps(enemy.getCollisionBounds())) return false;
        }

        return !getDestructibleObjectSystem().overlapsAny(targetBounds);
    }

    @Override
    public CraftingResult tryCraft(String recipeId) {
        Recipe recipe = craftingManager.getRecipeById(recipeId);
        if (recipe == null) throw new IllegalArgumentException("Recipe " + recipeId + " does not exist.");
        CraftingResult result = craftingManager.craft(recipe, player.getInventory());

        if (result.isSuccess() && recipe.getResultType() == ItemType.BOAT_KIT && !progressionState.isBoatKitCrafted()){
            progressionState.markBoatKitCrafted();
            guidanceSystem.handleEvent(GameEvent.BOAT_KIT_CRAFTED, progressionState);
        }

        if (result.isSuccess() && result.getOverflowAmount() > 0){
            getWorldItemSystem().add(new WorldItem(player.getX(), player.getY(), recipe.getResultType(), result.getOverflowAmount()));
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

        getDestructibleObjectSystem().createAndAdd(worldX, worldY, itemType.getPlacedObjectType());
        if (player.getInventory().getItemTypeBySlot(selectedSlot) == null) cancelTargeting();
    }

    private void plantTarget(int tileX, int tileY, float worldX, float worldY){
        int selectedSlot = player.getSelectedHotbarSlot();
        ItemType itemType = player.getInventory().getItemTypeBySlot(selectedSlot);
        if (itemType == null) return;

        int removed = player.getInventory().removeFromSlot(selectedSlot, 1);
        if (removed == 0) return;

        if (itemType == ItemType.WHEAT_SEED) getFarmingSystem().plant(CropType.WHEAT, tileX, tileY);
        else if (itemType == ItemType.SAPLING) getFarmingSystem().plantSapling(tileX, tileY);

        if (player.getInventory().getItemTypeBySlot(selectedSlot) == null) cancelTargeting();
    }

    private void tillTarget(int tileX, int tileY, float worldX, float worldY){
        getFarmingSystem().till(tileX, tileY);
        player.getInventory().getSlot(player.getSelectedHotbarSlot()).reduceDurability(1);
    }

    private void handleMatureSaplings(){
        for (int x=0; x < getTileMap().getWidth(); x++){
            for (int y=0; y < getTileMap().getHeight(); y++){
                GrowablePlant plant = getFarmingSystem().getPlant(x, y);
                if (!(plant instanceof Sapling)) continue;
                if (plant.getGrowthStage() != GrowthStage.MATURE) continue;
                if (!isTreeSpawnPositionValid(x, y)) continue; // neko stoji na tileu, sačekaj

                getFarmingSystem().removePlant(x, y);
                getDestructibleObjectSystem().createAndAdd(getTileMap().tileToWorldX(x), getTileMap().tileToWorldY(y), DestructibleObjectType.TREE);
            }
        }
    }

    private boolean isTreeSpawnPositionValid(int tileX, int tileY){
        float worldX = getTileMap().tileToWorldX(tileX);
        float worldY = getTileMap().tileToWorldY(tileY);
        Rectangle treeBounds = new Rectangle(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE);

        if (treeBounds.overlaps(player.getCollisionBounds())) return false;

        for (Enemy enemy : getEnemies()) {
            if (treeBounds.overlaps(enemy.getCollisionBounds())) return false;
        }

        return true;
    }

    private void activateAreaAtDock(AreaID areaID){
        if (areaID == null) throw new IllegalArgumentException("Area id cannot be null.");

        AreaRuntime runtime = areaRuntimes.get(areaID);
        boolean firstVisit = runtime == null;

        if (firstVisit){
            runtime = new AreaRuntime(areaManager.getArea(areaID), player, progressionState);
            areaRuntimes.put(areaID, runtime);
        }

        areaManager.setCurrentArea(areaID);
        currentAreaRuntime = runtime;
        player.setAreaEnvironment(runtime.getTileMap(), runtime.getCollisionSystem());
        updateBoatVisibility();

        Vector2 dockArrival = runtime.getTileMap().getObjectPosition("Objects", "dock_arrival");
        checkpointX = dockArrival.x;
        checkpointY = dockArrival.y;

        activeChest = null;
        cancelTargeting();

        if (firstVisit){
            runtime.getDestructibleObjectSystem().spawnInitialResources(player);
        }
    }

    private void prepareAreaAfterTravel(){
        int currentDayCount = dayNightCycle.getDayCount();
        int departureDayCount = currentAreaRuntime.getLastDepartureDayCount();

        if (departureDayCount != -1 && departureDayCount != currentDayCount){
            getEnemies().clear();
            getProjectileSystem().replaceProjectiles(List.of());
            getEnemySpawnSystem().endNight();
        }

        if (dayNightCycle.isNight() && currentAreaRuntime.getOrdinaryNightDayCount() != currentDayCount){
            startOrdinaryNightForCurrentArea();
        }
    }

    @Override
    public boolean travelToArea(AreaID destination){
        if (destination == null) throw new IllegalArgumentException("Destination cannot be null");

        if (!progressionState.isBoatBuilt()) return false;
        if (!progressionState.isAreaUnlocked(destination)) return false;

        if (destination == areaManager.getCurrentAreaId()) return false;
        if (!isBoatTravelReady()) return false;
        if (!canAffordBoatTravel()) return false;

        currentAreaRuntime.markDeparture(dayNightCycle.getDayCount());

        if (!player.getWallet().spendGold(Config.BOAT_TRAVEL_GOLD)) return false;

        activateAreaAtDock(destination);
        prepareAreaAfterTravel();
        boatTravelCooldownRemaining = Config.BOAT_TRAVEL_COOLDOWN;
        return true;
    }

    public boolean isWorldMapOpenRequested(){
        return worldMapRequested;
    }

    public void clearWorldMapOpenRequest(){
        worldMapRequested = false;
    }

    private boolean tryUseBoat(){
        Rectangle boatInteraction = getTileMap().getObjectRectangle("SpecialRegions", "boat_interaction");

        if (!boatInteraction.overlaps(player.getCollisionBounds())) return false;

        if (!progressionState.isOldJettyFound()){
            progressionState.discoverOldJetty();
            guidanceSystem.handleEvent(GameEvent.OLD_JETTY_DISCOVERED, progressionState);
        }

        if (!progressionState.isBoatBuilt()){
            boolean removed = player.getInventory().removeItem(ItemType.BOAT_KIT, 1);
            if (!removed) return true;

            progressionState.buildBoat();
            updateBoatVisibility();
            guidanceSystem.handleEvent(GameEvent.BOAT_BUILT, progressionState);
            return true;
        }

        worldMapRequested = true;
        return true;
    }

    public void updateBoatVisibility(){
        getTileMap().setLayerVisible("Boat", progressionState.isBoatBuilt());
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

    public AreaManager getAreaManager() {return areaManager;}
    public List<Recipe> getRecipes(){
        return craftingManager.getRecipes();
    }
    public TileMap getTileMap(){return currentAreaRuntime.getTileMap();}
    public Player getPlayer(){return player;}
    public List<Enemy> getEnemies() { return currentAreaRuntime.getEnemies(); }
    public DayNightCycle getDayNightCycle() {return dayNightCycle;}
    public TargetMode getTargetMode() {return targetMode;}
    public Chest getActiveChest() {return activeChest;}
    public FarmingSystem getFarmingSystem() {return currentAreaRuntime.getFarmingSystem();}
    public ProjectileSystem getProjectileSystem() {return currentAreaRuntime.getProjectileSystem();}
    public double getTotalPlayTimeSeconds() {return totalPlayTimeSeconds;}
    public ProgressionState getProgressionState() {return progressionState;}
    public EnemySpawnSystem getEnemySpawnSystem() {return currentAreaRuntime.getEnemySpawnSystem();}
    public float getCheckpointX() {return checkpointX;}
    public float getCheckpointY() {return checkpointY;}
    public List<WorldItem> getGroundItems(){return currentAreaRuntime.getWorldItemSystem().getItems();}
    public List<DestructibleObject> getDestructibleObjects(){return currentAreaRuntime.getDestructibleObjectSystem().getObjects();}
    public DestructibleObjectSystem getDestructibleObjectSystem(){return currentAreaRuntime.getDestructibleObjectSystem();}
    public WorldItemSystem getWorldItemSystem(){return currentAreaRuntime.getWorldItemSystem();}
    public GuidanceSystem getGuidanceSystem(){return guidanceSystem;}

    @Override
    public AreaID getCurrentAreaId(){return areaManager.getCurrentAreaId();}
    @Override
    public boolean isAreaUnlocked(AreaID areaID){return progressionState.isAreaUnlocked(areaID);}
    @Override
    public int getGold(){return player.getWallet().getGold();}
    @Override
    public int getBoatTravelCost(){return Config.BOAT_TRAVEL_GOLD;}
    @Override
    public boolean isBoatTravelReady(){return boatTravelCooldownRemaining <= 0f;}
    @Override
    public boolean canAffordBoatTravel(){
        return player.getWallet().canAfford(Config.BOAT_TRAVEL_GOLD);
    }
    @Override
    public float getBoatTravelCooldownRemaining() {return boatTravelCooldownRemaining;}

    public void dispose(){
        for (AreaRuntime runtime : areaRuntimes.values()){runtime.dispose();}
    }
}
