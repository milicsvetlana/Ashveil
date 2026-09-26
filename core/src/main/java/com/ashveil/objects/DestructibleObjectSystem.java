package com.ashveil.objects;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.collision.MovementType;
import com.ashveil.entities.Player;
import com.ashveil.entities.enemies.Enemy;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.progression.ProgressionState;
import com.ashveil.world.TileMap;
import com.ashveil.world.WorldItem;
import com.ashveil.world.WorldItemSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.rmi.server.ServerNotActiveException;
import java.util.*;

public class DestructibleObjectSystem {
    private final TileMap tileMap;
    private final CollisionSystem collisionSystem;
    private final ProgressionState progressionState;
    private final WorldItemSystem worldItemSystem;

    private final List<DestructibleObject> destructibleObjects;
    private final Map<DestructibleObjectType, ItemType> destructibleObjectDrops;

    private final Random random;

    public DestructibleObjectSystem(TileMap tileMap, CollisionSystem collisionSystem, ProgressionState progressionState, WorldItemSystem worldItemSystem){
        if (tileMap == null) throw new IllegalArgumentException("TileMap cannot be null.");
        if (collisionSystem == null) throw new IllegalArgumentException("CollisionSystem cannot be null.");
        if (progressionState == null) throw new IllegalArgumentException("ProgressionState cannot be null.");
        if (worldItemSystem == null) throw new IllegalArgumentException("WorldItemSystem cannot be null.");

        this.tileMap = tileMap;
        this.collisionSystem = collisionSystem;
        this.progressionState = progressionState;
        this.worldItemSystem = worldItemSystem;

        destructibleObjects = new ArrayList<>();
        random = new Random();

        destructibleObjectDrops = Map.of(
            DestructibleObjectType.TREE, ItemType.WOOD,
            DestructibleObjectType.ROCK, ItemType.STONE,
            DestructibleObjectType.FENCE, ItemType.FENCE,
            DestructibleObjectType.THORN_FENCE, ItemType.THORN_FENCE,
            DestructibleObjectType.CHEST, ItemType.CHEST,
            DestructibleObjectType.HOLLOWCAP, ItemType.HOLLOWCAP
        );
    }

    public void spawnInitialResources(Player player){
        spawnInitialResources(player, EnumSet.allOf(DestructibleObjectType.class));
    }

    public void spawnInitialResources(Player player, Set<DestructibleObjectType> allowedTypes){
        if (player == null) throw new IllegalArgumentException("Player cannot be null.");
        if (allowedTypes == null || allowedTypes.isEmpty()) return;

        List<DestructibleObjectType> naturalTypes = new ArrayList<>();

        for (DestructibleObjectType type : DestructibleObjectType.values()) {
            if (type.spawnsNaturally() && allowedTypes.contains(type)) naturalTypes.add(type);
        }

        for (DestructibleObjectType type : naturalTypes) {
            for (int i = 0; i < Config.MIN_INITIAL_RESOURCES; i++) spawnNaturalObject(type, player);
        }

        int numberOfExtraResources = random.nextInt(Config.MAX_EXTRA_INITIAL_RESOURCES + 1);

        for (int i = 0; i < numberOfExtraResources; i++) {
            DestructibleObjectType randomType = naturalTypes.get(random.nextInt(naturalTypes.size()));
            spawnNaturalObject(randomType, player);
        }
    }

    public DestructibleObject createAndAdd(float worldX, float worldY, DestructibleObjectType type){
        return createAndAdd(worldX, worldY, type, type.getHp());
    }

    public DestructibleObject createAndAdd(float worldX, float worldY, DestructibleObjectType type, int currentHp){
        if (type == null) throw new IllegalArgumentException("Destructible object type cannot be null.");
        DestructibleObject object = createObject(worldX, worldY, type, currentHp);
        add(object);
        return object;
    }

    public Chest createAndAddChest(float worldX, float worldY, ChestKind chestKind){
        return createAndAddChest(worldX, worldY, DestructibleObjectType.CHEST.getHp(), chestKind);
    }

    public Chest createAndAddChest(float worldX, float worldY, int currentHp, ChestKind kind) {
        Chest chest = new Chest(worldX, worldY, currentHp, kind);
        add(chest);
        return chest;
    }

    public void add(DestructibleObject object){
        if (object == null) throw new IllegalArgumentException("Destructible object cannot be null.");
        if (destructibleObjects.contains(object)) return;
        destructibleObjects.add(object);
        collisionSystem.register(object);
    }

    public void processDestroyedObjects(){
        for (DestructibleObject object : destructibleObjects) {
            if (!object.isDestroyed()) continue;
            dropDestroyedObjectItems(object);
            collisionSystem.unregister(object);
        }
        destructibleObjects.removeIf(DestructibleObject::isDestroyed);
    }

    public Chest findNearestChest(float x, float y, float range){
        Chest nearestChest = null;
        Double nearestDistanceSquared = null;

        float rangeSquared = range * range;

        for (DestructibleObject object : destructibleObjects){
            if (object.getType() != DestructibleObjectType.CHEST) continue;

            Rectangle bounds = object.getCollisionBounds();

            float chestCenterX = bounds.x + bounds.width / 2f;
            float chestCenterY = bounds.y + bounds.height / 2f;

            float dimX = chestCenterX - x;
            float dimY = chestCenterY - y;
            double distanceSquared = dimX * dimX + dimY * dimY;

            if (distanceSquared > rangeSquared) continue;

            if (nearestDistanceSquared == null || distanceSquared < nearestDistanceSquared){
                nearestDistanceSquared = distanceSquared;
                nearestChest = (Chest) object;
            }
        }

        return nearestChest;
    }

    public boolean overlapsAny(Rectangle bounds){
        if (bounds == null) throw new IllegalArgumentException("Bounds cannot be null.");
        for (DestructibleObject object : destructibleObjects){
            if (bounds.overlaps(object.getCollisionBounds())) return true;
        }
        return false;
    }

    private void spawnNaturalObject(DestructibleObjectType type, Player player){
        int tileX;
        int tileY;

        do {
            tileX = random.nextInt(tileMap.getWidth());
            tileY = random.nextInt(tileMap.getHeight());
        }
        while (!isNaturalSpawnPositionValid(tileX, tileY, player));
        createAndAdd(tileX * Config.TILE_SIZE, tileY * Config.TILE_SIZE, type);
    }

    private DestructibleObject createObject(float worldX, float worldY, DestructibleObjectType type, int currentHp){
        if (type == DestructibleObjectType.CHEST) return new Chest(worldX, worldY, currentHp);
        if (type == DestructibleObjectType.BRIAR_SNARE) return new BriarSnare(worldX, worldY, currentHp);
        if (type == DestructibleObjectType.HOLLOWCAP) return new Hollowcap(worldX, worldY, currentHp);
        return new DestructibleObject(worldX, worldY, type, currentHp);
    }

    private boolean isNaturalSpawnPositionValid(int tileX, int tileY, Player player){
        if (tileMap.isBlocked(tileX, tileY)) return false;
        if (tileMap.isNaturalSpawnBlocked(tileX, tileY)) return false;

        float worldX = tileX * Config.TILE_SIZE;
        float worldY = tileY * Config.TILE_SIZE;

        for (DestructibleObject object : destructibleObjects){
            if (object.getX() == worldX && object.getY() == worldY) return false;
        }

        int playerTileX = (int) (player.getX() / Config.TILE_SIZE);
        int playerTileY = (int) (player.getY() / Config.TILE_SIZE);
        int distanceFromPlayerX = Math.abs(tileX - playerTileX);
        int distanceFromPlayerY = Math.abs(tileY - playerTileY);

        return distanceFromPlayerX > Config.INITIAL_SPAWN_CLEAR_RADIUS || distanceFromPlayerY > Config.INITIAL_SPAWN_CLEAR_RADIUS;
    }

    private void dropDestroyedObjectItems(DestructibleObject object){
        ItemType dropType = destructibleObjectDrops.get(object.getType());
        if (dropType == null) return; //npr. Briar Snare ne dropuje nista

        int dropAmount = getDropAmount(object);

        worldItemSystem.add(new WorldItem(getRandomDropX(object), getRandomDropY(object), destructibleObjectDrops.get(object.getType()), dropAmount));

        if (object.getType() == DestructibleObjectType.TREE){
            int saplingAmount = random.nextInt(100) < 75 ? 1 : 2;

            worldItemSystem.add(new WorldItem(getRandomDropX(object), getRandomDropY(object), ItemType.SAPLING, saplingAmount));
        }

        if (object.getType() == DestructibleObjectType.CHEST) dropChestContents((Chest) object);
    }

    private void dropChestContents(Chest chest){
        for (int i = 0; i < chest.getChestInventory().getSize(); i++){
            ItemStack itemStack = chest.getChestInventory().getSlot(i);
            if (itemStack == null) continue;

            worldItemSystem.add(new WorldItem(getRandomDropX(chest), getRandomDropY(chest), itemStack));
        }
    }

    private int getDropAmount(DestructibleObject object){
        if (object.getType() == DestructibleObjectType.TREE && !progressionState.isFirstTreeDropClaimed()){
            progressionState.claimFirstTreeDrop();
            return Config.FIRST_TREE_DROP_AMOUNT;
        }

        return random.nextInt(object.getType().getMaxDrop() - object.getType().getMinDrop() + 1)
                                + object.getType().getMinDrop();
    }

    private float getRandomDropX(DestructibleObject object){
        return object.getX() + (random.nextInt(3) - 1) * Config.TILE_SIZE;
    }

    private float getRandomDropY(DestructibleObject object){
        return object.getY() + (random.nextInt(3) - 1) * Config.TILE_SIZE;
    }

    public void processBriarSnareTrigger(List<Enemy> enemies) {
        for (DestructibleObject object : destructibleObjects){
            if (!(object instanceof BriarSnare snare)) continue;
            if (snare.isDestroyed()) continue;;

            for (Enemy enemy : enemies){
                if (!enemy.isAlive()) continue;
                if (enemy.getMovementType() != MovementType.GROUND) continue;
                if (!snare.getCollisionBounds().overlaps(enemy.getCollisionBounds())) continue;

                enemy.receiveHit(Config.BRIAR_SNARE_DAMAGE);
                if (enemy.isAlive()) enemy.applyRoot(Config.BRIAR_SNARE_ROOT_TIMER);
                snare.trigger();
                break;
            }
        }
    }

    public void spawnObjectsInRegions(DestructibleObjectType type, List<Rectangle> regions, int amount, Player player){
        if (type == null) throw new IllegalArgumentException("Destructible object type cannot be null.");
        if (regions == null || regions.isEmpty()) return;

        int spawned = 0;
        int attempts = 0;
        int maxAttempts = amount * 50;

        while (spawned < amount && attempts < maxAttempts){
            attempts++;

            Rectangle region = regions.get(random.nextInt(regions.size()));

            int minTileX = (int) Math.ceil(region.x / Config.TILE_SIZE);
            int minTileY = (int) Math.ceil(region.y / Config.TILE_SIZE);

            int maxTileX = (int) Math.floor((region.x + region.width) / Config.TILE_SIZE) - 1;
            int maxTileY = (int) Math.floor((region.y + region.height) / Config.TILE_SIZE) - 1;

            if (maxTileX < minTileX || maxTileY < minTileY) continue;

            int tileX = minTileX + random.nextInt(maxTileX - minTileX + 1);
            int tileY = minTileY + random.nextInt(maxTileY - minTileY + 1);

            if (!isNaturalSpawnPositionValid(tileX, tileY, player)) continue;

            createAndAdd(tileX * Config.TILE_SIZE, tileY * Config.TILE_SIZE, type);

            spawned++;
        }

    }

    public List<DestructibleObject> getObjects(){
        return destructibleObjects;
    }

}

