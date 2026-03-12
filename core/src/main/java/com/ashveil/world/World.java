package com.ashveil.world;

import com.ashveil.Config;
import com.ashveil.entities.Entity;
import com.ashveil.entities.Player;
import com.ashveil.entities.ZombieEnemy;
import com.ashveil.items.Inventory;
import com.ashveil.items.ItemType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.ArrayList;
import java.util.List;

public class World {

    private TileMap tileMap;
    private Player player;
    private List<ZombieEnemy> zombies;
    private List<WorldItem> groundItems;

    public World(){
        tileMap = new TileMap();
        player = new Player(
            Config.WORLD_WIDTH * Config.TILE_SIZE / 2f,
            Config.WORLD_HEIGHT * Config.TILE_SIZE / 2f,
            6, 6,
            tileMap);
        zombies = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            float zx = Config.WORLD_WIDTH * Config.TILE_SIZE / 2f + (i * 50f);
            float zy = Config.WORLD_HEIGHT * Config.TILE_SIZE / 2f + 100f;
            zombies.add(new ZombieEnemy(zx, zy, 3, 3, player));
        }
        groundItems = new ArrayList<>();
        groundItems.add(new WorldItem(Config.WORLD_WIDTH * Config.TILE_SIZE / 2f + 50f,
                                Config.WORLD_WIDTH * Config.TILE_SIZE / 2f + 50f,
                                ItemType.WOOD, 2));
        groundItems.add(new WorldItem(Config.WORLD_HEIGHT * Config.TILE_SIZE / 2f + 150f,
            Config.WORLD_WIDTH * Config.TILE_SIZE / 2f + 50f,
            ItemType.STONE, 2));
    }

    public void update(float delta){
        player.update(delta);
        for (ZombieEnemy z : zombies) {
            z.update(delta);
        }

        for (ZombieEnemy z : zombies){
            if (isColliding(player, z)){
                player.takeDamage(1);
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.K) && player.canAttack()){
            player.attack(zombies);
            player.resetAttackCooldown();
            zombies.removeIf(ZombieEnemy::isDead);
        }

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

    public TileMap getTileMap(){return tileMap;}
    public Player getPlayer(){return player;}
    public List<ZombieEnemy> getZombies() { return zombies; }
    public List<WorldItem> getGroundItems() {return groundItems;}
}
