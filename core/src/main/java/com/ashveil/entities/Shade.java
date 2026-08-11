package com.ashveil.entities;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.world.TileMap;

public class Shade extends Enemy{

    public Shade(float x, float y, Player target, CollisionSystem collisionSystem) {
        super(x, y, EnemyType.SHADE, target, collisionSystem);
    }

    @Override
    protected void updateAlive(float delta) {
        moveTowardTarget(delta);
        if (isTouchingTarget()){
            attackTarget();
        }
    }

    private boolean isTouchingTarget(){
        return getCollisionBounds().overlaps(target.getCollisionBounds());
    }

    private void attackTarget(){
        target.takeDamage(enemyType.getDamage());
    }
}
