package com.ashveil.collision;

import com.ashveil.world.TileMap;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class CollisionSystem {
    private final TileMap tileMap;
    private final List<CollidableObject> collidableObjects;
    private final Rectangle testedBounds;

    public CollisionSystem(TileMap tileMap) {
        this.tileMap = tileMap;
        this.collidableObjects = new ArrayList<>();
        this.testedBounds = new Rectangle();
    }

    public void register(CollidableObject object){
        if (collidableObjects.contains(object)) return;
        collidableObjects.add(object);
    }

    public void unregister(CollidableObject object){
        collidableObjects.remove(object);
    }

    public boolean isBlocked(float x, float y, float width, float height, MovementType movementType){
        testedBounds.set(x, y, width, height);

        for (CollidableObject object : collidableObjects){
            if (testedBounds.overlaps(object.getCollisionBounds()) && object.blocksMovement(movementType)) return true; //ugradjena metoda overlaps
        }

        int firstTileX = tileMap.worldToTileX(x);
        int firstTileY = tileMap.worldToTileY(y);

        int lastTileX = tileMap.worldToTileX(x + width - 0.001f);
        int lastTileY = tileMap.worldToTileY(y + height - 0.001f);

        for (int tileX = firstTileX; tileX <= lastTileX; tileX++) {
            for (int tileY = firstTileY; tileY <= lastTileY; tileY++) {
                if (tileMap.isBlocked(tileX, tileY)) {
                    return true;
                }
            }
        }

        return false;
    }

}












