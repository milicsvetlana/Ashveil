package com.ashveil.entities;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.items.inventory.Inventory;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.world.TileMap;

import java.util.List;

public class Player extends Entity{

    private final TileMap tileMap;
    private final Inventory inventory;
    private final CollisionSystem collisionSystem;

    private float damageCooldown = 0f;
    private float primaryActionCooldown  = 0f;

    private int selectedHotbarSlot;

    public Player(float x, float y, TileMap tileMap, CollisionSystem collisionSystem) {
        super(x, y, Config.PLAYER_HP, Config.PLAYER_SPEED);
        this.tileMap = tileMap;
        this.collisionSystem = collisionSystem;
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
        return collisionSystem.isBlocked(px, py, Config.TILE_SIZE, Config.TILE_SIZE);
    }

    @Override
    public void takeDamage(int amount){
        if (damageCooldown > 0) return;
        damageCooldown = Config.DAMAGE_COOLDOWN_MAX;
        super.takeDamage(amount);
    }

    public float getFacingX() {
        return switch (facing) {
            case LEFT -> -1f;
            case RIGHT -> 1f;
            default -> 0f;
        };
    }

    public float getFacingY() {
        return switch (facing) {
            case DOWN -> -1f;
            case UP -> 1f;
            default -> 0f;
        };
    }

    public int pickUp(ItemType itemType, int amount){
        return inventory.addItem(itemType, amount);
    }

    public boolean canUsePrimaryAction(){return primaryActionCooldown <= 0;}
    public void resetPrimaryActionCooldown() {primaryActionCooldown = Config.PLAYER_PRIMARY_ACTION_COOLDOWN;}

    public Facing getFacing() {return facing;}
    public Inventory getInventory() {return inventory;}
    public int getSelectedHotbarSlot() {
        return selectedHotbarSlot;
    }

    public void setSelectedHotbarSlot(int selectedHotbarSlot) {
        if (selectedHotbarSlot < 0 ||  selectedHotbarSlot >= Config.HOTBAR_SIZE) return;
        this.selectedHotbarSlot = selectedHotbarSlot;
    }
}
