package com.ashveil.entities.enemies;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.entities.Player;
import com.ashveil.navigation.DistanceField;

public class Shade extends Enemy{
    private final DistanceField distanceField;
    private float moveDirX;
    private float moveDirY;
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1},
                                               {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    public Shade(float x, float y, Player target, CollisionSystem collisionSystem, DistanceField distanceField) {
        super(x, y, EnemyType.SHADE, target, collisionSystem);
        this.distanceField = distanceField;
    }

    @Override
    protected void updateAlive(float delta) {
        move(moveDirX, moveDirY, delta);
        if (isTouchingTarget()) attackTarget();
    }

    @Override
    protected void updateAiDecision() {
        int currentX = distanceField.worldToTileX(getCenterX());
        int currentY = distanceField.worldToTileY(getCenterY());

        moveDirX = 0;
        moveDirY = 0;
        int bestDistance = distanceField.getDistance(currentX, currentY);

        for (int[] direction : DIRECTIONS) {
            int dirX = direction[0];
            int dirY = direction[1];

            int neighborX = currentX + dirX;
            int neighborY = currentY + dirY;

            int neighborDistance = distanceField.getDistance(neighborX, neighborY);
            if (neighborDistance == DistanceField.UNREACHABLE) continue;

            if (dirX != 0 && dirY != 0) {
                if (distanceField.isNavigationBlocked(currentX + dirX, currentY)
                    || distanceField.isNavigationBlocked(currentX, currentY + dirY)) continue;
            }

            if (bestDistance == DistanceField.UNREACHABLE || neighborDistance < bestDistance) {
                bestDistance = neighborDistance;
                moveDirX = dirX;
                moveDirY = dirY;
            }
        }
    }

    private void attackTarget(){
        target.takeDamage(enemyType.getDamage());
    }

    private boolean isTouchingTarget(){
        return getCollisionBounds().overlaps(target.getCollisionBounds());
    }
}
