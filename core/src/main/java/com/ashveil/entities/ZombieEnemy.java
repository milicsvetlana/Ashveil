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
        float dir_x = target.getX() - x;
        float dir_y = target.getY() - y;

        float length = (float) Math.sqrt(dir_x * dir_x + dir_y * dir_y);
        if (length > 0){
            dir_x = dir_x / length;
            dir_y = dir_y / length;
        }

        x += dir_x * speed * delta;
        y += dir_y * speed * delta;

        if (Math.abs(dir_x) > Math.abs(dir_y)) {
            facing = dir_x > 0 ? Facing.RIGHT : Facing.LEFT;
        } else {
            facing = dir_y > 0 ? Facing.UP : Facing.DOWN;
        }
    }

}
