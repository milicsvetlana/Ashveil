package com.ashveil.world;

import com.ashveil.Config;
import com.ashveil.entities.Entity;
import com.ashveil.entities.Player;
import com.ashveil.entities.ZombieEnemy;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.ArrayList;
import java.util.List;

public class World {

    private TileMap tileMap;
    private Player player;
    private List<ZombieEnemy> zombies;

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
}
