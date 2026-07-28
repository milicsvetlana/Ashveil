package com.ashveil.entities;

import com.ashveil.Config;
import com.ashveil.items.inventory.Inventory;
import com.ashveil.objects.ResourceObject;
import com.ashveil.world.TileMap;
import com.ashveil.world.WorldItem;

import java.util.List;

public class Player extends Entity{

    private final TileMap tileMap;
    private final Inventory inventory;

    private float damageCooldown = 0f;
    private float primaryActionCooldown  = 0f;

    private int selectedHotbarSlot;

    public Player(float x, float y, TileMap tileMap) {
        super(x, y, Config.PLAYER_HP, Config.PLAYER_SPEED);
        this.tileMap = tileMap;
        inventory = new Inventory();
        selectedHotbarSlot = 0;
    }

    @Override
    public void update(float delta) {
        if (damageCooldown > 0) damageCooldown -= delta;
        if (primaryActionCooldown  > 0) primaryActionCooldown  -= delta;
    }

    public void move(float dx, float dy, float delta) {
        if (dx > 0) facing = Facing.RIGHT;
        else if (dx < 0) facing = Facing.LEFT;
        else if (dy > 0) facing = Facing.UP;
        else if (dy < 0) facing = Facing.DOWN;

        //deo koji normalizuje dijagonalno kretanje, jer bi se inače
        //dijagonalno kretao 41% brže, (koren iz 2 naspram 1)
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length > 0f) {
            dx /= length;
            dy /= length;
        }

        float speedMultiplier = tileMap.getMovementMultiplierAtWorld(x, y);

        float newX = x + dx * speed * speedMultiplier * delta;
        float newY = y + dy * speed * speedMultiplier * delta;


        //to sto se kolizija proverava odvojeno za X i Y je dobro zato
        //sto omogucava bolje kretanje uz zid - da ne blokira ako drzimo
        //i desno i gore, da, ako je iznad nas zid, i dalje mozemo desno
        if (!isCollidingAt(newX, y)) {
            x = newX;
        }
        if (!isCollidingAt(x, newY)) {
            y = newY;
        }
    }

    private boolean isCollidingAt(float px, float py) {
        int size = Config.TILE_SIZE;

        float rightX = px + size - 1;
        float topY = py + size - 1;

        return tileMap.isBlockedAtWorld(px, py)
            || tileMap.isBlockedAtWorld(rightX, py)
            || tileMap.isBlockedAtWorld(px, topY)
            || tileMap.isBlockedAtWorld(rightX, topY);
    }

    @Override
    public void takeDamage(int amount){
        if (damageCooldown > 0) {return;}
        currentHp -= amount;
        if (currentHp < 0) currentHp = 0;
        damageCooldown = Config.DAMAGE_COOLDOWN_MAX;
    }

    private float getFacingX() {
        return switch (facing) {
            case LEFT -> -1f;
            case RIGHT -> 1f;
            default -> 0f;
        };
    }

    private float getFacingY() {
        return switch (facing) {
            case DOWN -> -1f;
            case UP -> 1f;
            default -> 0f;
        };
    }

    private boolean isTargetInFrontCone(float targetCenterX, float targetCenterY, float range, float minDot) {
        float dx = targetCenterX - getCenterX();
        float dy = targetCenterY - getCenterY();

        float distSq = dx * dx + dy * dy;
        if (distSq > range * range) return false;
        if (distSq == 0f) return true;

        float invLen = 1f / (float)Math.sqrt(distSq);
        dx *= invLen;
        dy *= invLen;

        float dot = dx * getFacingX() + dy * getFacingY();
        return dot >= minDot;
    }

    public void attack(List<Shade> shades){
        for (Shade z : shades){
            if (!isTargetInFrontCone(
                z.getCenterX(),
                z.getCenterY(),
                Config.PLAYER_ATTACK_HARVEST_RANGE,
                Config.PLAYER_ATTACK_MIN_DOT
            )) continue;

            z.takeDamage(1);
        }
    }

    public void harvest(List<ResourceObject> objects){
        for (ResourceObject o : objects){
            if (!isTargetInFrontCone(
                o.getCenterX(),
                o.getCenterY(),
                Config.PLAYER_ATTACK_HARVEST_RANGE,
                Config.PLAYER_HARVEST_MIN_DOT
            )) continue;

            o.hit(1);
        }
    }

    public WorldItem pickUp(List<WorldItem> groundItems){
        for (WorldItem i : groundItems){
            float dimX = i.getX() - x;
            float dimY = i.getY() - y;
            double dist = Math.sqrt(dimX * dimX + dimY * dimY);
            if (dist > Config.PLAYER_PICKUP_RANGE) continue;

            boolean added = inventory.addItem(i.getType(), i.getAmount());
            if (added) return i;
        }
        return null;
    }


    public boolean canUsePrimaryAction(){return primaryActionCooldown <= 0;}
    public void resetPrimaryActionCooldown() {primaryActionCooldown = Config.PLAYER_PRIMARY_ACTION_COOLDOWN;}

    public Facing getFacing() {return facing;}
    public int getCurrentHp() {return currentHp;}
    public int getMaxHp() {return maxHp;}
    public Inventory getInventory() {return inventory;}
    public int getSelectedHotbarSlot() {
        return selectedHotbarSlot;
    }

    public void setSelectedHotbarSlot(int selectedHotbarSlot) {
        if (selectedHotbarSlot < 0 ||  selectedHotbarSlot >= Config.HOTBAR_SIZE) return;
        this.selectedHotbarSlot = selectedHotbarSlot;
    }
}
