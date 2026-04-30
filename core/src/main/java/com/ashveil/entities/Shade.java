package com.ashveil.entities;

import com.ashveil.Config;

public class Shade extends Enemy{

    public Shade(float x, float y, Player target) {
        super(x, y, Config.SHADE_HP, Config.SHADE_SPEED, target);
    }

    @Override
    public void update(float delta) {
        moveTowardTarget(delta);
    }

}
