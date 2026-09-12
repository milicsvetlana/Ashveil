package com.ashveil.objects;

import com.ashveil.Config;

public abstract class WorldObject {
    int maxHp;
    int currentHp;

    float x;
    float y;

    public WorldObject(float x, float y, int maxHp){
        this.x = x;
        this.y = y;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
    }

    public WorldObject(float x, float y, int maxHp, int currentHp){
        if (maxHp <= 0) throw new IllegalArgumentException("Max HP must be positive.");
        if (currentHp <= 0 || currentHp > maxHp) throw new IllegalArgumentException("Invalid current HP.");

        this.x = x;
        this.y = y;
        this.maxHp = maxHp;
        this.currentHp = currentHp;
    }

    public void hit(int amount){
        currentHp -= amount;
        if (currentHp < 0) currentHp = 0;
    }

    public boolean isDestroyed(){return currentHp <= 0;}

    public float getX() {return x;}
    public float getY() {return y;}
    public float getCenterX() {return x + Config.TILE_SIZE / 2f;}
    public float getCenterY() {return y + Config.TILE_SIZE / 2f;}
    public int getCurrenthp() {return currentHp;}
}
