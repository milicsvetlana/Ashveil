package com.ashveil.entities;

import com.ashveil.Config;
import com.ashveil.items.Inventory;
import com.ashveil.objects.ResourceObject;
import com.ashveil.objects.ResourceType;
import com.ashveil.world.TileMap;
import com.ashveil.world.WorldItem;

import java.util.List;

public class Player extends Entity{

    private TileMap tileMap;
    private Inventory inventory;

    private float damageCooldown = 0f;
    private float attackCooldown = 0f;
    private float harvestCooldown = 0f;

    public Player(float x, float y, TileMap tileMap) {
        super(x, y, Config.PLAYER_HP, Config.PLAYER_SPEED);
        this.tileMap = tileMap;
        inventory = new Inventory();
    }

    @Override
    public void update(float delta) {
        if (damageCooldown > 0) damageCooldown -= delta;
        if (attackCooldown > 0) attackCooldown -= delta;
        if (harvestCooldown > 0) harvestCooldown -= delta;
    }

    public void move(float dx, float dy, float delta) {
        float newX = x + dx * speed * delta;
        float newY = y + dy * speed * delta;

        if (dx > 0) facing = Facing.RIGHT;
        else if (dx < 0) facing = Facing.LEFT;
        else if (dy > 0) facing = Facing.UP;
        else if (dy < 0) facing = Facing.DOWN;

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
            if (dist > Config.PLAYER_ATTACK_HARVEST_RANGE) continue;
            z.takeDamage(1);
        }
    }

    public void harvest(List<ResourceObject> objects){
        for (ResourceObject o : objects){
            if (facing == Facing.UP && o.getY() <= y){continue;}
            if (facing == Facing.DOWN && o.getY() >= y){continue;}
            if (facing == Facing.RIGHT && o.getX() <= x){continue;}
            if (facing == Facing.LEFT && o.getX() >= x){continue;}

            float dx = o.getX() - x;
            float dy = o.getY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > Config.PLAYER_ATTACK_HARVEST_RANGE) continue;
            o.hit(1);
        }
    }

    public WorldItem pickUp(List<WorldItem> groundItems){
        for (WorldItem i : groundItems){
            float dimX = i.getX() - x;
            float dimY = i.getY() - y;
            double dist = Math.sqrt(dimX * dimX + dimY * dimY);
            if (dist > Config.PLAYER_PICKUP_RANGE) continue;
            inventory.addItem(i.getType(), i.getAmount());
            return i;
        }
        return null;
    }


    public boolean canAttack(){return attackCooldown <= 0;}
    public void resetAttackCooldown() {attackCooldown = Config.PLAYER_ATTACK_COOLDOWN;}

    public boolean canHarvest(){return harvestCooldown <= 0;}
    public void resetHarvestCooldown() {harvestCooldown = Config.PLAYER_HARVEST_COOLDOWN;}

    public Facing getFacing() {return facing;}
    public int getCurrentHp() {return currentHp;}
    public int getMaxHp() {return maxHp;}
    public Inventory getInventory() {return inventory;}
}
