package com.ashveil.entities.enemies;

import com.ashveil.collision.MovementType;

public enum EnemyType {
    SHADE(6, 30, 1, MovementType.GROUND, 1),
    WISP(6, 30, 1, MovementType.FLYING, 2),
    WRAITH(6, 30, 2, MovementType.GROUND, 3);

    private final int maxHp;
    private final float maxSpeed;
    private final int damage;
    private final MovementType movementType;
    private final int threatCost;

    EnemyType(int maxHp, float maxSpeed, int damage, MovementType movementType, int threatCost){
        this.maxHp = maxHp;
        this.maxSpeed = maxSpeed;
        this.damage = damage;
        this.movementType = movementType;
        this.threatCost = threatCost;
    }

    public int getMaxHp() {return maxHp;}
    public float getMaxSpeed() {return maxSpeed;}
    public int getDamage() {return damage;}
    public MovementType getMovementType() {return movementType;}
    public int getThreatCost() {return threatCost;}
}
