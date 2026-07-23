package com.ashveil.entities;

import com.ashveil.Config;
import com.ashveil.world.TileMap;

public class Shade extends Enemy{

    public Shade(float x, float y, Player target, TileMap tilemap) {
        super(x, y, Config.SHADE_HP, Config.SHADE_SPEED, target, tilemap);

    }

    @Override
    public void update(float delta) {
        moveTowardTarget(delta);
    }

}
