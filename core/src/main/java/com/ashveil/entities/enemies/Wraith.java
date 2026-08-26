package com.ashveil.entities.enemies;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.combat.ProjectileSystem;
import com.ashveil.entities.Player;
import com.ashveil.navigation.DistanceField;
import com.ashveil.navigation.NavigationMode;

public class Wraith extends Enemy{
    private final DistanceField distanceField;
    private final ProjectileSystem projectileSystem;

    private WraithState wraithState;
    private float attackCooldown;

    private int nextTileX;
    private int nextTileY;
    private boolean hasNextTile;

    private enum WraithState{
        APPROACH,
        RETREAT,
        ATTACK;
    }
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1},
                                               {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    public Wraith(float x, float y, Player target, CollisionSystem collisionSystem, DistanceField distanceField, ProjectileSystem projectileSystem) {
        super(x, y, EnemyType.WRAITH, target, collisionSystem);
        this.distanceField = distanceField;
        this.projectileSystem = projectileSystem;

        wraithState = WraithState.APPROACH;
        attackCooldown = 0f;

        nextTileX = 0;
        nextTileY = 0;
        hasNextTile = false;
    }

    @Override
    protected void updateAlive(float delta) {
        if (attackCooldown > 0) attackCooldown -= delta;
        switch (wraithState){
            case APPROACH, RETREAT -> {
                if (!hasNextTile) selectNextTile();
                if (!hasNextTile) return;

                float targetX = distanceField.tileToWorldX(nextTileX);
                float targetY = distanceField.tileToWorldY(nextTileY);

                if (moveTowardPoint(targetX, targetY, delta)){
                    hasNextTile = false;
                    selectNextTile();
                }
            }

            case ATTACK -> {
                hasNextTile = false;

                if (attackCooldown <= 0){
                    fireProjectile();
                    attackCooldown = Config.WRAITH_ATTACK_COOLDOWN;
                }
            }
        }
    }

    @Override
    protected void updateAiDecision() {
        float dirX = target.getCenterX() - getCenterX();
        float dirY = target.getCenterY() - getCenterY();

        WraithState newState;

        float distance = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (distance <= Config.WRAITH_RETREAT_RANGE) newState = WraithState.RETREAT;
        else if (distance <= Config.WRAITH_ATTACK_RANGE) newState = WraithState.ATTACK;
        else newState = WraithState.APPROACH;

        if (newState != wraithState){
            wraithState = newState;
            hasNextTile = false;
        }
    }
    //imala sam problem sa samo biranjem smera jer su enemiji kacili na coskovima, pa je cilj postao naredni tile
    private void selectNextTile() {
        int currentX = distanceField.worldToTileX(getCenterX());
        int currentY = distanceField.worldToTileY(getCenterY());

        int bestX = currentX;
        int bestY = currentY;
        int bestDistance = distanceField.getDistance(currentX, currentY, NavigationMode.NORMAL);

        if (bestDistance == DistanceField.UNREACHABLE) {
            hasNextTile = false;
            return;
        }

        for (int[] direction : DIRECTIONS) {
            int dirX = direction[0];
            int dirY = direction[1];

            int neighborX = currentX + dirX;
            int neighborY = currentY + dirY;

            int neighborDistance = distanceField.getDistance(neighborX, neighborY, NavigationMode.NORMAL);
            if (neighborDistance == DistanceField.UNREACHABLE) continue;

            if (dirX != 0 && dirY != 0) {
                if (distanceField.isNavigationBlocked(currentX + dirX, currentY, NavigationMode.NORMAL)
                    || distanceField.isNavigationBlocked(currentX, currentY + dirY, NavigationMode.NORMAL)) continue;
            }

            boolean betterTile = wraithState == WraithState.APPROACH ? neighborDistance < bestDistance : neighborDistance > bestDistance;

            if (betterTile) {
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

    private void fireProjectile(){
        float dirX = target.getCenterX() - getCenterX();
        float dirY = target.getCenterY() - getCenterY();

        float projectileSize = Config.TILE_SIZE / 2;

        float spawnX = getCenterX() - projectileSize / 2f;
        float spawnY = getCenterY() - projectileSize / 2f;

        projectileSystem.spawnProjectile(spawnX, spawnY, dirX, dirY, Config.WRAITH_PROJECTILE_SPEED, enemyType.getDamage(), Config.WRAITH_PROJECTILE_LIFETIME);
    }
}




















