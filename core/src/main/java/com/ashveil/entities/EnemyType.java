package com.ashveil.entities;

public enum EnemyType {
    SHADE(6, 30, 1),
    WISP(6, 30, 1),
    WRAITH(6, 30, 2);

    private final int maxHp;
    private float maxSpeed;
    private final int damage;

    EnemyType(int maxHp, float maxSpeed, int damage){
        this.maxHp = maxHp;
        this.maxSpeed = maxSpeed;
        this.damage = damage;
    }

    public int getMaxHp() {return maxHp;}
    public float getMaxSpeed() {return maxSpeed;}
    public int getDamage() {return damage;}
}
