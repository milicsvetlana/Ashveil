package com.ashveil.world;

import com.ashveil.Config;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class TileMap {
    private final TiledMap tiledMap;
    private final TiledMapTileLayer collisionLayer;

    private final int width;
    private final int height;

    public TileMap(){
        tiledMap = new TmxMapLoader().load("maps/test_map.tmx");

        //posto getlayers vraca opsti maplayer, mi kastujemo
        collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Collision");

        width = tiledMap.getProperties().get("width", Integer.class);
        height = tiledMap.getProperties().get("height", Integer.class);
    }

    public boolean isBlocked(int x, int y){
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        return collisionLayer.getCell(x, y) != null;
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void dispose() {
        tiledMap.dispose();
    }
}
