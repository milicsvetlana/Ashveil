package com.ashveil.entities;

public abstract class Enemy extends Entity{

    protected final Player target;
    float hitFlashTimer = 0f;

    public Enemy(float x, float y, int maxHp, float speed, Player target) {
        super(x, y, maxHp, speed);
        this.target = target;
    }

    protected void moveTowardTarget(float delta) {
        if (hitFlashTimer > 0) hitFlashTimer -= delta;

        float dirX = target.getX() - x;
        float dirY = target.getY() - y;

        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (length > 0){
            dirX = dirX / length;
            dirY = dirY / length;
        }

        x += dirX * speed * delta;
        y += dirY * speed * delta;

        if (Math.abs(dirX) > Math.abs(dirY)) {
            facing = dirX > 0 ? Facing.RIGHT : Facing.LEFT;
        } else {
            facing = dirY > 0 ? Facing.UP : Facing.DOWN;
        }
    }

    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);
        hitFlashTimer = 0.15f;
    }

    public float getHitFlashTimer() {
        return hitFlashTimer;
    }
}
