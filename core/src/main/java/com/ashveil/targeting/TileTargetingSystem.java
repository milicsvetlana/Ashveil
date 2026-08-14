package com.ashveil.targeting;

import com.ashveil.Config;
import com.ashveil.world.CameraController;
import com.ashveil.world.TileMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

//sluzi samo da kaze o kome se tileu radi
public class TileTargetingSystem {
    private int tileX;
    private int tileY;
    private float worldX;
    private float worldY;

    private final CameraController cameraController;
    private final TileMap tileMap;

    public TileTargetingSystem(CameraController cameraController, TileMap tileMap) {
        this.cameraController = cameraController;
        this.tileMap = tileMap;
    }

    public void update(){
        Vector3 mouseWorldCoordinates = cameraController.screenToWorld(Gdx.input.getX(), Gdx.input.getY());

        float logicalWorldX = mouseWorldCoordinates.x / Config.SCALE;
        float logicalWorldY = mouseWorldCoordinates.y / Config.SCALE;

        tileX = tileMap.worldToTileX(logicalWorldX);
        tileY = tileMap.worldToTileY(logicalWorldY);

        worldX = tileX * Config.TILE_SIZE;
        worldY = tileY * Config.TILE_SIZE;
    }

    public int getTileX() {return tileX;}
    public int getTileY() {return tileY;}
    public float getWorldX() {return worldX;}
    public float getWorldY() {return worldY;}
}
