package com.ashveil.entities;

public enum EnemyType {
    SHADE(6, 30),
    WISP(6, 30),
    WRAITH(6, 30);

    private final int maxHp;
    private final float maxSpeed;

    EnemyType(int maxHp, float maxSpeed){
        this.maxHp = maxHp;
        this.maxSpeed = maxSpeed;
    }

    public int getMaxHp() {return maxHp;}
    public float getMaxSpeed() {return maxSpeed;}
}
