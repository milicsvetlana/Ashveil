package com.ashveil.entities;

import com.ashveil.Config;
import com.ashveil.world.TileMap;

public class Shade extends Enemy{

    public Shade(float x, float y, Player target, TileMap tilemap) {
        super(x, y, EnemyType.SHADE, target, tilemap);
    }

    @Override
    protected void updateAlive(float delta) {
        moveTowardTarget(delta);
    }
}
