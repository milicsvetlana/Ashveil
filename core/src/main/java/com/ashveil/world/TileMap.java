package com.ashveil.world;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class TileMap {
    private final TiledMap tiledMap;
    private final TiledMapTileLayer collisionLayer;
    private final TiledMapTileLayer groundLayer;

    private final int width;
    private final int height;
    private final int tileWidth;
    private final int tileHeight;

    private final float playerSpawnX;
    private final float playerSpawnY;

    public TileMap(){
        tiledMap = new TmxMapLoader().load("maps/test_map.tmx");

        //posto getlayers vraca opsti maplayer, mi kastujemo
        collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Collision");
        groundLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Ground");

        width = tiledMap.getProperties().get("width", Integer.class);
        height = tiledMap.getProperties().get("height", Integer.class);
        tileWidth = tiledMap.getProperties().get("tilewidth", Integer.class);
        tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);

        MapLayer objectsLayer = tiledMap.getLayers().get("Objects");
        MapObject playerSpawn = objectsLayer.getObjects().get("player_spawn");

        playerSpawnX = playerSpawn.getProperties().get("x", Float.class);
        playerSpawnY = playerSpawn.getProperties().get("y", Float.class);
    }

    public boolean isBlocked(int x, int y){
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        return collisionLayer.getCell(x, y) != null;
    }

    public int worldToTileX(float worldX){return (int) Math.floor(worldX / tileWidth);}
    public int worldToTileY(float worldY){return (int) Math.floor(worldY / tileHeight);}

    public float tileToWorldX(int tileX){return tileX * tileWidth;}
    public float tileToWorldY(int tileY){return tileY * tileHeight;}

    public boolean isBlockedAtWorld (float worldX, float worldY){
        return isBlocked(worldToTileX(worldX), worldToTileY(worldY));
    }

    public float getMovementMultiplierAtWorld(float x, float y) {
        TiledMapTileLayer.Cell cell = groundLayer.getCell(worldToTileX(x), worldToTileY(y));
        if (cell == null) return 1f;
        Float multiplier = cell.getTile().getProperties().get("movementMultiplier", Float.class);
        if (multiplier == null) return 1f;
        return multiplier;
    }

    public boolean isTillable(float x, float y){
        int tileX = worldToTileX(x);
        int tileY = worldToTileY(y);
        TiledMapTileLayer.Cell cell = groundLayer.getCell(worldToTileX(x), worldToTileY(y));
        if (cell == null) return false;
        Boolean tillable = cell.getTile().getProperties().get("tillable", Boolean.class);
        return tillable != null && tillable;
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }
    public int getWidth() {return width;}
    public int getHeight() {return height;}
    public float getPlayerSpawnX() {return playerSpawnX;}
    public float getPlayerSpawnY() {return playerSpawnY;}

    public void dispose() {
        tiledMap.dispose();
    }
}
