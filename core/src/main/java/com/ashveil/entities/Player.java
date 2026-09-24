package com.ashveil.entities;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.collision.MovementType;
import com.ashveil.economy.Wallet;
import com.ashveil.items.inventory.Inventory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.world.TileMap;

import java.util.List;

public class Player extends Entity{
    private String characterName;

    private TileMap tileMap;
    private CollisionSystem collisionSystem;
    private final Inventory inventory;
    private final Wallet wallet;

    private float damageCooldown = 0f;
    private float primaryActionCooldown  = 0f;

    private int selectedHotbarSlot;
    private int brokenHearts;

    private float dashCooldown = 0f;

    public Player(float x, float y) {
        super(x, y, Config.PLAYER_HP, Config.PLAYER_SPEED, MovementType.GROUND);

        characterName = "Unnamed";
        inventory = new Inventory();
        wallet = new Wallet();
        selectedHotbarSlot = 0;
        brokenHearts = 0;
    }

    public void setAreaEnvironment(TileMap tileMap, CollisionSystem collisionSystem){
        if (tileMap == null) throw new IllegalArgumentException("Tile map cannot be null.");
        if (collisionSystem == null) throw new IllegalArgumentException("Collision system cannot be null.");

        this.tileMap = tileMap;
        this.collisionSystem = collisionSystem;
    }

    @Override
    public void update(float delta) {
        if (damageCooldown > 0) damageCooldown -= delta;
        if (primaryActionCooldown  > 0) primaryActionCooldown  -= delta;
        if (dashCooldown > 0f) dashCooldown = Math.max(0f, dashCooldown - delta);
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
        return collisionSystem.isBlocked(px, py, Config.TILE_SIZE, Config.TILE_SIZE, getMovementType());
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

    public void addBrokenHeart(){
        if (brokenHearts >= Config.MAX_BROKEN_HEARTS) return;
        brokenHearts++;
        maxHp = Config.PLAYER_HP - brokenHearts * Config.HP_PER_HEART;
    }

    public boolean repairBrokenHeart(){
        if (brokenHearts == 0) return false;
        brokenHearts--;
        maxHp = Config.PLAYER_HP - brokenHearts * Config.HP_PER_HEART;
        return true;
    }

    public void restoreHealth(){
        currentHp = maxHp;
    }

    public void useHeartRepair(){
        repairBrokenHeart();
        restoreHealth();
    }

    public void heal(int amount){
        int newHp = amount + currentHp;
        currentHp = Math.min(newHp, maxHp);
    }

    public boolean canUsePrimaryAction(){return primaryActionCooldown <= 0;}
    public void resetPrimaryActionCooldown() {primaryActionCooldown = Config.PLAYER_PRIMARY_ACTION_COOLDOWN;}
    public void resetAfterRespawn(){
        restoreHealth();
        damageCooldown = Config.DAMAGE_COOLDOWN_MAX;
        primaryActionCooldown = 0;
    }

    public void setSelectedHotbarSlot(int selectedHotbarSlot) {
        if (selectedHotbarSlot < 0 ||  selectedHotbarSlot >= Config.HOTBAR_SIZE) return;
        this.selectedHotbarSlot = selectedHotbarSlot;
    }

    public void setCharacterName(String characterName){
        if (characterName == null || characterName.isBlank()) throw new IllegalArgumentException("Name cannot be emptyl.");
        this.characterName = characterName;
    }

    public void applyPersistentState(int health, int brokenHearts, int gold, int selectedHotbarSlot, ItemStack[] inventoryContents) {
        if (brokenHearts < 0 || brokenHearts > Config.MAX_BROKEN_HEARTS) {
            throw new IllegalArgumentException("Invalid broken hearts count.");
        }
        int restoredMaxHp = Config.PLAYER_HP - brokenHearts * Config.HP_PER_HEART;

        if (health < 0 || health > restoredMaxHp) throw new IllegalArgumentException("Invalid player health");
        if (gold < 0) throw new IllegalArgumentException("Gold can't be negative");
        if (selectedHotbarSlot < 0 || selectedHotbarSlot >= Config.HOTBAR_SIZE)
            throw new IllegalArgumentException("Invalid hotbar slot.");
        if (inventoryContents == null || inventoryContents.length != Config.INVENTORY_SIZE)
            throw new IllegalArgumentException("Invalid inventory contents.");

        this.brokenHearts = brokenHearts;
        this.maxHp = restoredMaxHp;
        this.currentHp = health;
        this.selectedHotbarSlot = selectedHotbarSlot;
        inventory.replaceContents(inventoryContents);
        wallet.addGold(gold);
    }

    public boolean dash(float inputX, float inputY){
        if (dashCooldown > 0f) return false;

        float dashX = inputX;
        float dashY = inputY;

        float length = (float) Math.sqrt(dashX * dashX + dashY * dashY);

        if (length > 0f){
            dashX /= length;
            dashY /= length;
        }
        else {
            dashX = getFacingX();
            dashY = getFacingY();
        }

        float remainingDistance = Config.PLAYER_DASH_DISTANCE;
        float step = Config.TILE_SIZE / 4f;

        boolean moved = false;

        while (remainingDistance > 0f){
            float currentStep = Math.min(step, remainingDistance);
            float nextX = x + dashX * currentStep;
            float nextY = y + dashY * currentStep;
            boolean movedThisStep = false;

            if (!isCollidingAt(nextX, y)){
                x = nextX;
                movedThisStep = true;
            }

            if (!isCollidingAt(x, nextY)){
                y = nextY;
                movedThisStep = true;
            }

            if (!movedThisStep) break;

            moved = true;
            remainingDistance -= currentStep;
        }

        if (moved) dashCooldown = Config.PLAYER_DASH_COOLDOWN;
        return moved;
    }

    public Facing getFacing() {return facing;}
    public Inventory getInventory() {return inventory;}
    public int getSelectedHotbarSlot() {
        return selectedHotbarSlot;
    }
    public int getBrokenHearts(){return brokenHearts;}
    public Wallet getWallet(){return wallet;}
    public String getCharacterName(){return characterName;}
}
