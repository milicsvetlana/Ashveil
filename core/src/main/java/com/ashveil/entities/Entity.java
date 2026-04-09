package com.ashveil.entities;

import com.ashveil.Config;

public abstract class Entity {
    protected float x, y;
    int maxHp;
    int currentHp;
    protected float speed;
    protected Facing facing = Facing.DOWN;

    public Entity (float x, float y, int maxHp, float speed){
        this.x = x;
        this.y = y;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.speed = speed;
    }

    public void takeDamage(int amount){
        currentHp -= amount;
        if (currentHp < 0) currentHp = 0;
    }

    public boolean isDead(){
        return currentHp <= 0;
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

    public abstract void update(float delta);
}
