package com.ashveil.world;

import com.ashveil.Config;
import com.ashveil.entities.Entity;
import com.ashveil.entities.Player;
import com.ashveil.entities.Shade;
import com.ashveil.items.CraftingManager;
import com.ashveil.items.Recipe;
import com.ashveil.objects.ResourceObject;
import com.ashveil.objects.ResourceType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {

    private Random random = new Random();

    private TileMap tileMap;
    private DayNightCycle dayNightCycle;

    private CraftingManager craftingManager;
    private List<WorldItem> groundItems;
    private List<ResourceObject> resourceObjects;

    private Player player;
    private List<Shade> shades;

    public World(){
        tileMap = new TileMap();
        player = new Player(
            Config.WORLD_WIDTH * Config.TILE_SIZE / 2f,
            Config.WORLD_HEIGHT * Config.TILE_SIZE / 2f,
            tileMap);
        shades = new ArrayList<>();
        groundItems = new ArrayList<>();
        craftingManager = new CraftingManager();
        spawnObjects();

        dayNightCycle = new DayNightCycle();
    }

    public void update(float delta){
        player.update(delta);

        for (Shade z : shades){
            z.update(delta);
        }

        float dx = 0f;
        float dy = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx += 1f;

        player.move(dx, dy, delta);

        if (dayNightCycle.justBecameNight()){
            spawnShades();
        }

        handleCollisions();
        handleCombat();
        handleHarvesting();
        handlePickup();

        dayNightCycle.update(delta);
    }

    private void handleCollisions() {
        for (Shade z : shades){
            if (isColliding(player, z)){
                player.takeDamage(1);
            }
        }
    }

    private void handleCombat(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.K) && player.canAttack()){
            player.attack(shades);
            player.resetAttackCooldown();
            shades.removeIf(Shade::isDead);
        }
    }

    private void handleHarvesting(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.K) && player.canHarvest()){
            player.harvest(resourceObjects);
            player.resetHarvestCooldown();
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

    private void handlePickup() {
        if (Gdx.input.isKeyPressed(Input.Keys.E)){
            WorldItem toRemove = player.pickUp(groundItems);
            if (toRemove != null) groundItems.remove(toRemove);
        }
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

            shades.add(new Shade(zx * Config.TILE_SIZE, zy * Config.TILE_SIZE, player));
        }
    }

    private void spawnObjects(){
        int rx;
        int ry;
        int type;
        int numberOfItems = random.nextInt(101) + 50;
        resourceObjects = new ArrayList<>();
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
