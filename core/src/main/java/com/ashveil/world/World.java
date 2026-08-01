package com.ashveil.world;

import com.ashveil.Config;
import com.ashveil.combat.CombatSystem;
import com.ashveil.combat.Hittable;
import com.ashveil.entities.Entity;
import com.ashveil.entities.Player;
import com.ashveil.entities.Shade;
import com.ashveil.items.crafting.CraftingManager;
import com.ashveil.items.crafting.Recipe;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.objects.ResourceObject;
import com.ashveil.objects.ResourceType;
import com.ashveil.input.PlayerInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {

    private final Random random = new Random();

    private final TileMap tileMap;
    private final DayNightCycle dayNightCycle;

    private final CraftingManager craftingManager;
    private final CombatSystem combatSystem;
    private final List<WorldItem> groundItems;
    private final List<ResourceObject> resourceObjects;

    private final Player player;
    private final List<Shade> shades;

    public World(){
        tileMap = new TileMap();
        player = new Player(tileMap.getPlayerSpawnX(), tileMap.getPlayerSpawnY(), tileMap);
        shades = new ArrayList<>();
        resourceObjects = new ArrayList<>();
        groundItems = new ArrayList<>();
        craftingManager = new CraftingManager();
        combatSystem = new CombatSystem();
        spawnObjects();
        dayNightCycle = new DayNightCycle();
        addGroundItem(new WorldItem(player.getX(), player.getY(), ItemType.LORE_SCROLL, 1));
        addGroundItem(new WorldItem(player.getX(), player.getY(), ItemType.WOOD, 1));
        addGroundItem(new WorldItem(player.getX(), player.getY(), ItemType.STONE, 1));
        addGroundItem(new WorldItem(player.getX(), player.getY(), ItemType.BREAD, 1));
    }

    public void update(float delta, PlayerInput playerInput){
        player.update(delta);

        for (Shade z : shades){
            z.update(delta);
        }

        player.move(playerInput.getMoveX(), playerInput.getMoveY(), delta);

        if (dayNightCycle.justBecameNight()){
            spawnShades();
        }

        handleCollisions();
        handleHotbarSelection(playerInput);
        handlePrimaryAction(playerInput);
        handlePickup(playerInput);
        handleDropItem(playerInput);
        dayNightCycle.update(delta);

        for (WorldItem item : groundItems){
            item.update(delta);
        }

        groundItems.removeIf(WorldItem::shouldDespawn);
    }

    private void handleCollisions() {
        for (Shade z : shades){
            if (isColliding(player, z)){
                player.takeDamage(1);
            }
        }
    }

    private void handlePrimaryAction(PlayerInput playerInput){
        if (playerInput.isPrimaryActionPressed() && player.canUsePrimaryAction()){

            List<Hittable> targets = new ArrayList<>();
            targets.addAll(shades);
            targets.addAll(resourceObjects);

            int numberOfHits = combatSystem.performPrimaryAction(player, targets);
            player.resetPrimaryActionCooldown();

            for (ResourceObject o : resourceObjects){
                if (o.isDestroyed()){
                    addGroundItem(new WorldItem(o.getX() + (random.nextInt(3) - 1) * Config.TILE_SIZE,
                        o.getY() + (random.nextInt(3) - 1) * Config.TILE_SIZE, o.getType().getDrop(),
                                random.nextInt(o.getType().getMaxDrop() - o.getType().getMinDrop() + 1) + o.getType().getMinDrop()));
                }
            }

            resourceObjects.removeIf(ResourceObject::isDestroyed);
            shades.removeIf(Shade::isDead);
        }
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

    private boolean isColliding(Entity a, Entity b){
        return a.getX() < b.getX() + Config.TILE_SIZE &&
            a.getX() + Config.TILE_SIZE > b.getX() &&
            a.getY() < b.getY() + Config.TILE_SIZE &&
            a.getY() + Config.TILE_SIZE > b.getY();
    }

    private void spawnShades(){
        int zx;
        int zy;
        for (int i=0; i < dayNightCycle.getDayCount()*2; i++) {
            do {
                zx = random.nextInt(tileMap.getWidth());
                zy = random.nextInt(tileMap.getHeight());
            } while (tileMap.isBlocked(zx, zy));

            shades.add(new Shade(zx * Config.TILE_SIZE, zy * Config.TILE_SIZE, player, tileMap));
        }
    }

    private void spawnObjects(){
        int rx;
        int ry;
        int type;
        int numberOfItems = random.nextInt(50);
        for (int i=0; i < numberOfItems; i++) {
            do {
                rx = random.nextInt(tileMap.getWidth());
                ry = random.nextInt(tileMap.getHeight());
                type = random.nextInt(ResourceType.values().length);
            } while (tileMap.isBlocked(rx, ry));

            resourceObjects.add(new ResourceObject(rx * Config.TILE_SIZE, ry * Config.TILE_SIZE, ResourceType.values()[type]));
        }
    }

    private void addGroundItem(WorldItem newItem){
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

    public void tryCraft(Recipe recipe) {
        craftingManager.craft(recipe, player.getInventory());
    }

    public List<Recipe> getRecipes(){
        return craftingManager.getRecipes();
    }
    public TileMap getTileMap(){return tileMap;}
    public Player getPlayer(){return player;}
    public List<Shade> getShades() { return shades; }
    public List<WorldItem> getGroundItems() {return groundItems;}
    public List<ResourceObject> getResourceObjects() {return resourceObjects;}
    public DayNightCycle getDayNightCycle() {return dayNightCycle;}

    public void dispose(){
        tileMap.dispose();
    }
}
