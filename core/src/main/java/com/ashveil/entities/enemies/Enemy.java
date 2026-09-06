package com.ashveil.entities.enemies;

import com.ashveil.collision.CollisionSystem;
import com.ashveil.combat.HitCategory;
import com.ashveil.combat.Hittable;
import com.ashveil.Config;
import com.ashveil.entities.Entity;
import com.ashveil.entities.Facing;
import com.ashveil.entities.Player;

public abstract class Enemy extends Entity implements Hittable {

    protected final Player target;
    private final CollisionSystem collisionSystem;
    protected final EnemyType enemyType;
    private EnemyState state;
    private float hitFlashTimer = 0f;
    private float hpBarTimer;
    private float dyingTimer;
    private float aiDecisionTimer;
    private float fleeTargetX;
    private float fleeTargetY;
    private boolean fleeFinished;

    public Enemy(float x, float y, EnemyType enemyType, Player target, CollisionSystem collisionSystem) {
        super(x, y, enemyType.getMaxHp(), enemyType.getMaxSpeed(), enemyType.getMovementType());
        this.enemyType = enemyType;
        this.target = target;
        this.state = EnemyState.ALIVE;
        this.hitFlashTimer = 0;
        this.hpBarTimer = 0;
        this.dyingTimer = 0;
        this.collisionSystem = collisionSystem;
        this.fleeFinished = false;
    }

    public final void update(float delta){ //ova i naredna klasa su bitne jer ovde stavljamo final kako ne bi moglo da se nasledi,
                                           //a imamo odvojenu metodu updateAlive koja ce biti nasledjena
        if (hitFlashTimer > 0) hitFlashTimer -= delta;
        if (hpBarTimer > 0) hpBarTimer -= delta;

        if (state == EnemyState.DYING){
            dyingTimer -= delta;
            return;
        }

        if (state == EnemyState.FLEEING){
            updateFleeing(delta);
            return;
        }

        aiDecisionTimer -= delta;
        if (aiDecisionTimer <= 0){
            updateAiDecision();
            aiDecisionTimer = Config.ENEMY_AI_DECISION_INTERVAL;
        }
        updateAlive(delta);
    }

    @Override
    public void takeDamage(int amount) {
        if (state != EnemyState.ALIVE) return;
        super.takeDamage(amount);
        hitFlashTimer = Config.ENEMY_HIT_FLASH_DURATION;
        hpBarTimer = Config.ENEMY_HP_BAR_DURATION;
        if (isDead()){
            state = EnemyState.DYING;
            dyingTimer = Config.ENEMY_DYING_DURATION;
            hpBarTimer = 0;
        }
    }

    protected abstract void updateAlive(float delta);

    public boolean shouldBeRemoved(){
        return (state == EnemyState.DYING && dyingTimer <= 0) || (state == EnemyState.FLEEING && fleeFinished);
    }

    @Override
    public boolean canReceiveHit(){
        return state == EnemyState.ALIVE;
    }

    protected void moveInDirection(float dirX, float dirY, float delta) {
        moveInDirection(dirX, dirY, delta, speed);
    }

    protected void moveInDirection(float dirX, float dirY, float delta, float movementSpeed){
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (length == 0) return;
        dirX = dirX / length;
        dirY = dirY / length;

        float newX = x + dirX * movementSpeed * delta;
        float newY = y + dirY * movementSpeed * delta;

        if (!isCollidingAt(newX, y)) x = newX;
        if (!isCollidingAt(x, newY)) y = newY;

        if (Math.abs(dirX) > Math.abs(dirY)) facing = dirX > 0 ? Facing.RIGHT : Facing.LEFT;
        else facing = dirY > 0 ? Facing.UP : Facing.DOWN;
    }

    protected abstract void updateAiDecision();

    protected boolean moveTowardPoint(float targetX, float targetY, float delta) {
        float dirX = targetX - x;
        float dirY = targetY - y;

        float distance = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (distance <= 0.001f) {
            x = targetX;
            y = targetY;
            return true;
        }

        dirX /= distance;
        dirY /= distance;

        float step = Math.min(speed * delta, distance);

        float newX = x + dirX * step;
        float newY = y + dirY * step;

        if (!isCollidingAt(newX, y)) x = newX;
        if (!isCollidingAt(x, newY)) y = newY;

        if (Math.abs(dirX) > Math.abs(dirY)) facing = dirX > 0 ? Facing.RIGHT : Facing.LEFT;
        else facing = dirY > 0 ? Facing.UP : Facing.DOWN;

        return Math.abs(x - targetX) <= 0.001f && Math.abs(y - targetY) <= 0.001f;
    }

    public void startFleeing(float worldWidth, float worldHeight){
        if (state != EnemyState.ALIVE) return;

        float minDistance = this.getX();

        fleeTargetX = -Config.TILE_SIZE;
        fleeTargetY = getY();
        facing = Facing.LEFT;

        if (minDistance > worldWidth - this.getX()){
            minDistance = worldWidth - this.getX();

            fleeTargetX = worldWidth + Config.TILE_SIZE;
            fleeTargetY = getY();
            facing = Facing.RIGHT;
        }
        if (minDistance > this.getY()){
            minDistance = this.getY();

            fleeTargetX = getX();
            fleeTargetY = -Config.TILE_SIZE;
            facing = Facing.DOWN;
        }
        if (minDistance > worldHeight - this.getY()){
            fleeTargetX = getX();
            fleeTargetY = worldHeight + Config.TILE_SIZE;
            facing = Facing.UP;
        }

        fleeFinished = false;
        state = EnemyState.FLEEING;
    }

    private void updateFleeing(float delta){
        float dirX = fleeTargetX - x;
        float dirY = fleeTargetY - y;

        float distance = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        float step = Config.ENEMY_FLEE_SPEED * delta;

        if (distance <= step) {
            x = fleeTargetX;
            y = fleeTargetY;
            fleeFinished = true;
            return;
        }

        dirX /= distance;
        dirY /= distance;
        x += dirX * step;
        y += dirY * step;
    }

    @Override
    public void receiveHit(int amount) {takeDamage(amount);}

    private boolean isCollidingAt(float px, float py){
        return collisionSystem.isBlocked(px, py, Config.TILE_SIZE, Config.TILE_SIZE, getMovementType());
    }

    @Override
    public HitCategory getHitCategory() {return HitCategory.ENTITY;}
    public EnemyType getEnemyType() {return enemyType;}
    public float getHitFlashTimer() {return hitFlashTimer;}
    protected CollisionSystem getCollisionSystem(){return collisionSystem;}
    public float getRenderAlpha(){return state == EnemyState.FLEEING ? 0.80f : 1f;}
    public boolean wasKilled(){return state == EnemyState.DYING;}
    public boolean isAlive(){return state == EnemyState.ALIVE;}
}
