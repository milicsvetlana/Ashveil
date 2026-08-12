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

    public Enemy(float x, float y, EnemyType enemyType, Player target, CollisionSystem collisionSystem) {
        super(x, y, enemyType.getMaxHp(), enemyType.getMaxSpeed(), enemyType.getMovementType());
        this.enemyType = enemyType;
        this.target = target;
        this.state = EnemyState.ALIVE;
        this.hitFlashTimer = 0;
        this.hpBarTimer = 0;
        this.dyingTimer = 0;
        this.collisionSystem = collisionSystem;
    }

    public final void update(float delta){ //ova i naredna klasa su bitne jer ovde stavljamo final kako ne bi moglo da se nasledi,
                                           //a imamo odvojenu metodu updateAlive koja ce biti nasledjena
        if (hitFlashTimer > 0) hitFlashTimer -= delta;
        if (hpBarTimer > 0) hpBarTimer -= delta;

        if (state == EnemyState.DYING){
            dyingTimer -= delta;
            return;
        }

        updateAlive(delta);
    }

    @Override
    public void takeDamage(int amount) {
        if (state == EnemyState.DYING) return;
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
        return state == EnemyState.DYING && dyingTimer <= 0;
    }

    @Override
    public boolean canReceiveHit(){
        return state == EnemyState.ALIVE;
    }

    protected void moveTowardTarget(float delta) {
        float dirX = target.getX() - x;
        float dirY = target.getY() - y;

        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);

        if (length > 0){
            dirX = dirX / length;
            dirY = dirY / length;
        }

        float newX = x + dirX * speed * delta;
        float newY = y + dirY * speed * delta;

        if (!isCollidingAt(newX, y)) x = newX;
        if (!isCollidingAt(x, newY)) y = newY;

        if (Math.abs(dirX) > Math.abs(dirY)) facing = dirX > 0 ? Facing.RIGHT : Facing.LEFT;
        else facing = dirY > 0 ? Facing.UP : Facing.DOWN;
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
}
