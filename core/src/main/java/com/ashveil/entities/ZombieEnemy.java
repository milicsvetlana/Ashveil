package com.ashveil.entities;

import com.ashveil.Config;

public class ZombieEnemy extends Entity{

    private final Player target;

    public ZombieEnemy(float x, float y, int maxHp, int currentHp, Player target) {
        super(x, y, maxHp, currentHp, Config.ZOMBIE_SPEED);
        this.target = target;
    }

    @Override
    public void update(float delta) {
        float dirX = target.getX() - x;
        float dirY = target.getY() - y;

        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (length > 0){
            dirX = dirX/ length;
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

}
