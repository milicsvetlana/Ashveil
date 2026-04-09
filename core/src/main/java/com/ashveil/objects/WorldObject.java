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

    public void hit(int amount){
        currentHp -= amount;
        if (currentHp < 0) currentHp = 0;
    }

    public boolean isDestroyed(){return currentHp <= 0;}

    public float getX() {return x;}
    public float getY() {return y;}
    public float getCenterX() {return x + Config.TILE_SIZE / 2f;}
    public float getCenterY() {return y + Config.TILE_SIZE / 2f;}
}
