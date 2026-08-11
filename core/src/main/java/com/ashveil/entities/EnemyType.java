package com.ashveil.entities;

import com.ashveil.collision.MovementType;

public enum EnemyType {
    SHADE(6, 30, 1, MovementType.GROUND),
    WISP(6, 30, 1, MovementType.FLYING),
    WRAITH(6, 30, 2, MovementType.GROUND);

    private final int maxHp;
    private float maxSpeed;
    private final int damage;
    private final MovementType movementType;

    EnemyType(int maxHp, float maxSpeed, int damage, MovementType movementType){
        this.maxHp = maxHp;
        this.maxSpeed = maxSpeed;
        this.damage = damage;
        this.movementType = movementType;
    }

    public int getMaxHp() {return maxHp;}
    public float getMaxSpeed() {return maxSpeed;}
    public int getDamage() {return damage;}
    public MovementType getMovementType() {return movementType;}
}
