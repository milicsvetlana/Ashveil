package com.ashveil.entities;

import com.ashveil.Config;
import com.ashveil.collision.MovementType;
import com.ashveil.combat.Hittable;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {
    protected float x, y;
    int maxHp;
    int currentHp;
    protected float speed;
    protected Facing facing = Facing.DOWN;
    private final Rectangle collisionBounds;
    private final MovementType movementType;

    public Entity (float x, float y, int maxHp, float speed, MovementType movementType){
        this.x = x;
        this.y = y;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.speed = speed;
        collisionBounds = new Rectangle(x, y, Config.TILE_SIZE, Config.TILE_SIZE);
        this.movementType = movementType;
    }

    public void takeDamage(int amount){
        currentHp -= amount;
        if (currentHp < 0) currentHp = 0;
    }

    public boolean isDead(){
        return currentHp <= 0;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

    public float getCenterX() {
        return x + Config.TILE_SIZE / 2f;
    }
    public float getCenterY() {
        return y + Config.TILE_SIZE / 2f;
    }
    public int getCurrentHp() {return currentHp;}
    public int getMaxHp() {return maxHp;}
    public MovementType getMovementType() {return movementType;}

    public Rectangle getCollisionBounds() {
        collisionBounds.setPosition(x, y);
        return collisionBounds;
    }

    public abstract void update(float delta);
}
