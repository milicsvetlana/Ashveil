package com.ashveil.entities;

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

    public abstract void update(float delta);
}
