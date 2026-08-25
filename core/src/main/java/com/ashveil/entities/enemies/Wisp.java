package com.ashveil.entities.enemies;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.entities.Player;

public class Wisp extends Enemy {
    private WispState wispState;
    private float stateTimer;

    private float dashDirX;
    private float dashDirY;

    private boolean damagedPlayerThisDash;

    private enum WispState{
        APPROACH,
        CHARGING,
        DASHING,
        RECOVERING;
    }

    public Wisp(float x, float y, Player target, CollisionSystem collisionSystem) {
        super(x, y, EnemyType.WISP, target, collisionSystem);
        wispState = WispState.APPROACH;
        stateTimer = 0f;
        dashDirX = 0f;
        dashDirY = 0f;
        damagedPlayerThisDash = false;
    }

    @Override
    protected void updateAlive(float delta) {
        switch (wispState){
            case APPROACH -> {
                float dirX = target.getCenterX() - getCenterX();
                float dirY = target.getCenterY() - getCenterY();
                moveInDirection(dirX, dirY, delta);

            }
            case CHARGING -> {
                stateTimer -= delta;
                if (stateTimer <= 0){
                    dashDirX = target.getCenterX() - getCenterX();
                    dashDirY = target.getCenterY() - getCenterY();

                    float length = (float) Math.sqrt(dashDirX * dashDirX + dashDirY * dashDirY);

                    if (length > 0) {
                        dashDirX /= length;
                        dashDirY /= length;
                    }

                    damagedPlayerThisDash = false;
                    wispState = WispState.DASHING;
                    stateTimer = Config.WISP_DASH_DURATION;
                }
            }
            case DASHING -> {
                stateTimer -= delta;
                moveInDirection(dashDirX, dashDirY, delta, Config.WISP_DASH_SPEED);

                if (!damagedPlayerThisDash && getCollisionBounds().overlaps(target.getCollisionBounds())){
                    target.takeDamage(enemyType.getDamage());
                    damagedPlayerThisDash = true;
                    wispState = WispState.RECOVERING;
                    stateTimer = Config.WISP_RECOVERY_DURATION;
                }

                if (stateTimer <= 0){
                    wispState = WispState.RECOVERING;
                    stateTimer = Config.WISP_RECOVERY_DURATION;
                }
            }
            case RECOVERING -> {
                stateTimer -= delta;
                if (stateTimer <= 0) wispState = WispState.APPROACH;
            }
        }
    }

    @Override
    protected void updateAiDecision() {
        if(wispState != WispState.APPROACH) return;
        float dirX = target.getCenterX() - getCenterX();
        float dirY = target.getCenterY() - getCenterY();

        float distance = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (distance <= Config.WISP_CHARGE_RANGE) {
            wispState = WispState.CHARGING;
            stateTimer = Config.WISP_CHARGE_DURATION;
        }
    }

    public boolean isCharging(){
        return wispState == WispState.CHARGING;
    }

}
