package com.ashveil.entities;

import com.ashveil.Config;
import com.ashveil.world.TileMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.List;

public class Player extends Entity{

    private TileMap tileMap;

    private float damageCooldown = 0f;
    private float attackCooldown = 0f;

    public Player(float x, float y, int maxHp, int currentHp, TileMap tileMap) {
        super(x, y, maxHp, currentHp, Config.PLAYER_SPEED);
        this.tileMap = tileMap;
    }

    @Override
    public void update(float delta) {
        if (damageCooldown > 0) damageCooldown -= delta;
        if (attackCooldown > 0) attackCooldown -= delta;

        float newX = x;
        float newY = y;

        if (Gdx.input.isKeyPressed(Input.Keys.W)){
            newY += speed * delta;
            facing = Facing.UP;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            newX -= speed * delta;
            facing = Facing.LEFT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            newY -= speed * delta;
            facing = Facing.DOWN;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            newX += speed * delta;
            facing = Facing.RIGHT;
        }

        int tileX = (int)(newX / Config.TILE_SIZE);
        int tileY = (int)(newY / Config.TILE_SIZE);

        if (!isCollidingAt(newX, y)) {
            x = newX;
        }

        if (!isCollidingAt(x, newY)) {
            y = newY;
        }
    }

    private boolean isCollidingAt(float px, float py) {
        int size = Config.TILE_SIZE;
        return tileMap.isBlocked((int)(px / size), (int)(py / size))
            || tileMap.isBlocked((int)((px + size - 1) / size), (int)(py / size))
            || tileMap.isBlocked((int)(px / size), (int)((py + size - 1) / size))
            || tileMap.isBlocked((int)((px + size - 1) / size), (int)((py + size - 1) / size));
    }

    @Override
    public void takeDamage(int amount){
        if (damageCooldown > 0) {return;}
        currentHp -= amount;
        if (currentHp < 0) currentHp = 0;
        damageCooldown = Config.DAMAGE_COOLDOWN_MAX;
    }

    public void attack(List<ZombieEnemy> zombies){
        for (ZombieEnemy z : zombies){
            if (facing == Facing.UP && z.getY() <= y){continue;}
            if (facing == Facing.DOWN && z.getY() >= y){continue;}
            if (facing == Facing.RIGHT && z.getX() <= x){continue;}
            if (facing == Facing.LEFT && z.getX() >= x){continue;}

            float dx = z.getX() - x;
            float dy = z.getY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > Config.PLAYER_ATTACK_RANGE) continue;
            z.takeDamage(1);
        }
    }

    public boolean canAttack(){return attackCooldown <= 0;}
    public void resetAttackCooldown() {attackCooldown = Config.PLAYER_ATTACK_COOLDOWN;}

    public Facing getFacing() {return facing;}
    public int getCurrentHp() {return currentHp;}
    public int getMaxHp() {return maxHp;}
}
