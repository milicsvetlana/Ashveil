package com.ashveil.world;

import com.ashveil.Config;
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
    private final List<WorldItem> groundItems;
    private final List<ResourceObject> resourceObjects;

    private final Player player;
    private final List<Shade> shades;

    public World(){
        tileMap = new TileMap();
        player = new Player(
            Config.WORLD_WIDTH * Config.TILE_SIZE / 2f,
            Config.WORLD_HEIGHT * Config.TILE_SIZE / 2f,
            tileMap);
        shades = new ArrayList<>();
        groundItems = new ArrayList<>();
        craftingManager = new CraftingManager();
        resourceObjects = new ArrayList<>();
        spawnObjects();

        dayNightCycle = new DayNightCycle();
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
            player.attack(shades);
            shades.removeIf(Shade::isDead);
            player.harvest(resourceObjects);
            player.resetPrimaryActionCooldown();
            for (ResourceObject o : resourceObjects){
                if (o.isDestroyed()){

                    groundItems.add(new WorldItem(o.getX() + (random.nextInt(3) - 1) * Config.TILE_SIZE,
                        o.getY() + (random.nextInt(3) - 1) * Config.TILE_SIZE,
                        o.getType().drop,
                        random.nextInt(o.getType().maxDrop - o.getType().minDrop + 1) + o.getType().minDrop));
                }
            }
            resourceObjects.removeIf(ResourceObject::isDestroyed);
        }
    }

    private void handlePickup(PlayerInput playerInput) {
        if (playerInput.isInteractPressed()){
            WorldItem toRemove = player.pickUp(groundItems);
            if (toRemove != null) groundItems.remove(toRemove);
        }
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

        player.getInventory().removeFromSlot(player.getSelectedHotbarSlot(), quantity);
        groundItems.add(new WorldItem(player.getX() + (random.nextInt(3) - 1) * Config.TILE_SIZE,
            player.getY() + (random.nextInt(3) - 1) * Config.TILE_SIZE, itemType, quantity));
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
                zx = random.nextInt(Config.WORLD_WIDTH);
                zy = random.nextInt(Config.WORLD_HEIGHT);
            } while (tileMap.getTile(zx, zy) == TileType.WATER);

            shades.add(new Shade(zx * Config.TILE_SIZE, zy * Config.TILE_SIZE, player, tileMap));
        }
    }

    private void spawnObjects(){
        int rx;
        int ry;
        int type;
        int numberOfItems = random.nextInt(101) + 50;
        for (int i=0; i < numberOfItems; i++) {
            do {
                rx = random.nextInt(Config.WORLD_WIDTH);
                ry = random.nextInt(Config.WORLD_HEIGHT);
                type = random.nextInt(ResourceType.values().length);
            } while (tileMap.getTile(rx, ry) == TileType.WATER);

            resourceObjects.add(new ResourceObject(rx * Config.TILE_SIZE, ry * Config.TILE_SIZE, ResourceType.values()[type]));
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
}
