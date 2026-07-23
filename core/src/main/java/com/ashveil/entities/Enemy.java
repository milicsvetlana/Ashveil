package com.ashveil.entities;

import com.ashveil.world.TileMap;
import com.ashveil.Config;

public abstract class Enemy extends Entity{

    protected final Player target;
    protected final TileMap tileMap;
    float hitFlashTimer = 0f;

    public Enemy(float x, float y, int maxHp, float speed, Player target, TileMap tileMap) {
        super(x, y, maxHp, speed);
        this.target = target;
        this.tileMap = tileMap;
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

        float newX = x + dirX * speed * delta;
        float newY = y + dirY * speed * delta;

        if (!isCollidingAt(newX, y)){
            x = newX;
        }

        if (!isCollidingAt(x, newY)) {
            y = newY;
        }

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

    private boolean isCollidingAt(float px, float py){
        int size = Config.TILE_SIZE;

        return tileMap.isBlocked((int) (px / size), (int) (py / size)) ||
            tileMap.isBlocked((int) ((px + size - 1) / size), (int) (py / size)) ||
            tileMap.isBlocked((int) (px / size), (int) ((py + size - 1) / size)) ||
            tileMap.isBlocked((int) ((px + size - 1) / size), (int) ((py + size - 1) / size)
        );
    }
}
