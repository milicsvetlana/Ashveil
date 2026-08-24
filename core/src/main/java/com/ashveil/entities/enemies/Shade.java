package com.ashveil.entities.enemies;

import com.ashveil.Config;
import com.ashveil.collision.CollidableObject;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.collision.MovementType;
import com.ashveil.entities.Player;
import com.ashveil.navigation.DistanceField;
import com.ashveil.navigation.NavigationMode;
import com.ashveil.objects.DestructibleObject;
import com.ashveil.objects.DestructibleObjectType;

public class Shade extends Enemy{
    private final DistanceField distanceField;
    private int nextTileX;
    private int nextTileY;
    private boolean hasNextTile;
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1},
                                               {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    private enum ShadeState{
        CHASE,
        ATTACK_PLAYER,
        ATTACK_FENCE
    }
    private ShadeState shadeState;
    private DestructibleObject targetFence;
    private float attackCooldown;

    public Shade(float x, float y, Player target, CollisionSystem collisionSystem, DistanceField distanceField) {
        super(x, y, EnemyType.SHADE, target, collisionSystem);
        this.distanceField = distanceField;
        shadeState = ShadeState.CHASE;
        targetFence = null;
        attackCooldown = 0f;
        hasNextTile = false;
    }

    @Override
    protected void updateAlive(float delta) {
        if (attackCooldown > 0) attackCooldown -= delta;

        switch (shadeState){
            case CHASE -> {
                if (isTouchingTarget()){
                    shadeState = ShadeState.ATTACK_PLAYER;
                    hasNextTile = false;
                    attackPlayer();
                    return;
                }

                if (!hasNextTile) selectNextTile();
                if (!hasNextTile) return;

                DestructibleObject blockingFence = findBlockingFence();

                if (blockingFence != null) {
                    targetFence = blockingFence;
                    shadeState = ShadeState.ATTACK_FENCE;
                    hasNextTile = false;
                    return;
                }

                float targetX = distanceField.tileToWorldX(nextTileX);
                float targetY = distanceField.tileToWorldY(nextTileY);

                if (moveToward(targetX, targetY, delta)){
                    hasNextTile = false;
                    selectNextTile();
                }
            }

            case ATTACK_PLAYER -> {
                if (!isTouchingTarget()){
                    shadeState = ShadeState.CHASE;
                    return;
                }
                attackPlayer();
            }

            case ATTACK_FENCE ->{
                if (targetFence == null || targetFence.isDestroyed()){
                    targetFence = null;
                    shadeState = ShadeState.CHASE;
                    hasNextTile = false;
                    return;
                }
                attackFence();
            }
        }
    }

    @Override
    protected void updateAiDecision() {
        if (isTouchingTarget()) {
            shadeState = ShadeState.ATTACK_PLAYER;
            targetFence = null;
            hasNextTile = false;
            return;
        }

        if (shadeState == ShadeState.ATTACK_FENCE) {
            if (targetFence != null && !targetFence.isDestroyed()) return;
            targetFence = null;
        }

        shadeState = ShadeState.CHASE;
    }

    private void selectNextTile() {
        int currentX = distanceField.worldToTileX(getCenterX());
        int currentY = distanceField.worldToTileY(getCenterY());

        NavigationMode navigationMode;

        if (distanceField.getDistance(currentX, currentY, NavigationMode.NORMAL) != DistanceField.UNREACHABLE) navigationMode = NavigationMode.NORMAL;
        else navigationMode = NavigationMode.BREAK_FENCES;

        int bestX = currentX;
        int bestY = currentY;

        int bestDistance = distanceField.getDistance(currentX, currentY, navigationMode);

        for (int[] direction : DIRECTIONS) {
            int dirX = direction[0];
            int dirY = direction[1];

            int neighborX = currentX + dirX;
            int neighborY = currentY + dirY;

            int neighborDistance = distanceField.getDistance(neighborX, neighborY, navigationMode);
            if (neighborDistance == DistanceField.UNREACHABLE) continue;

            if (dirX != 0 && dirY != 0) {
                if (distanceField.isNavigationBlocked(currentX + dirX, currentY, navigationMode)
                    || distanceField.isNavigationBlocked(currentX, currentY + dirY, navigationMode)) continue;
            }

            if (bestDistance == DistanceField.UNREACHABLE || neighborDistance < bestDistance) {
                bestDistance = neighborDistance;
                bestX = neighborX;
                bestY = neighborY;
            }
        }

        if (bestX == currentX && bestY == currentY) {
            hasNextTile = false;
            return;
        }

        nextTileX = bestX;
        nextTileY = bestY;
        hasNextTile = true;
    }

    private DestructibleObject findBlockingFence(){
        if (!hasNextTile) return null;

        float worldX = distanceField.tileToWorldX(nextTileX);
        float worldY = distanceField.tileToWorldY(nextTileY);

        CollidableObject blockingObject = getCollisionSystem().getBlockingObject(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE, MovementType.GROUND);

        if (!(blockingObject instanceof DestructibleObject object))return null;
        if (object.getType() != DestructibleObjectType.FENCE) return null;
        return object;
    }

    private void attackPlayer(){
        if (attackCooldown > 0) return;
        target.takeDamage(enemyType.getDamage());
        attackCooldown = Config.ENEMY_ATTACK_COOLDOWN;
    }

    private void attackFence(){
        if (attackCooldown > 0) return;
        targetFence.receiveHit(enemyType.getDamage());
        attackCooldown = Config.ENEMY_ATTACK_COOLDOWN;
    }

    private boolean isTouchingTarget(){
        return getCollisionBounds().overlaps(target.getCollisionBounds());
    }
}
