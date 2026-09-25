package com.ashveil.world;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class TileMap {
    private final TiledMap tiledMap;

    private final TiledMapTileLayer collisionLayer;
    private final TiledMapTileLayer groundLayer;
    private final TiledMapTileLayer hazardLayer;

    private final MapLayer objectsLayer;

    private final int width;
    private final int height;
    private final int tileWidth;
    private final int tileHeight;

    public TileMap(String mapPath){
        if (mapPath == null || mapPath.isBlank()) throw new IllegalArgumentException("Map Path cannot be null.");
        tiledMap = new TmxMapLoader().load(mapPath);

        //posto getlayers vraca opsti maplayer, mi kastujemo
        collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Collision");
        groundLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Ground");
        hazardLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Hazards");

        objectsLayer = tiledMap.getLayers().get("Objects");
        width = tiledMap.getProperties().get("width", Integer.class);
        height = tiledMap.getProperties().get("height", Integer.class);
        tileWidth = tiledMap.getProperties().get("tilewidth", Integer.class);
        tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);
    }

    public boolean isBlocked(int x, int y){
        return isOutOfBounds(x, y) || hasCollisionTile(x, y);
    }

    public boolean isNaturalSpawnBlocked(int tileX, int tileY){
        if (isOutOfBounds(tileX, tileY)) return true;
        if (isHazard(tileX, tileY)) return true;
        if (objectsLayer == null) return false;

        Rectangle tileBounds = new Rectangle(tileToWorldX(tileX), tileToWorldY(tileY), tileWidth, tileHeight);

        for (MapObject object : objectsLayer.getObjects()){
            if (!"NoNaturalSpawn".equals(objectsLayer.getName())) continue;
            if (!(object instanceof RectangleMapObject rectangleMapObject)) continue;
            if (tileBounds.overlaps(rectangleMapObject.getRectangle())) return true;
        }

        return false;
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

    public boolean isTreePlantable(int tileX, int tileY){
        if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
            return false;
        }

        TiledMapTileLayer.Cell cell = groundLayer.getCell(tileX, tileY);
        if (cell == null) return false;

        Boolean treePlantable =
            cell.getTile().getProperties().get("treePlantable", Boolean.class);

        return treePlantable != null && treePlantable;
    }

    public boolean isHazard(int tileX, int tileY){
        if (isOutOfBounds(tileX, tileY)) return false;
        if (hazardLayer == null) return false;

        TiledMapTileLayer.Cell cell = hazardLayer.getCell(tileX, tileY);
        if (cell == null) return false;
        if (cell.getTile() == null) return false;

        Boolean hazard = cell.getTile()
            .getProperties()
            .get("hazard", Boolean.class);

        return Boolean.TRUE.equals(hazard);
    }

    public boolean isHazardAtWorld(float worldX, float worldY){
        return isHazard(worldToTileX(worldX), worldToTileY(worldY));
    }

    public boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    public boolean hasCollisionTile(int x, int y) {
        if (isOutOfBounds(x, y)) return false;
        return collisionLayer.getCell(x, y) != null;
    }

    public Vector2 getObjectPosition(String layerName, String objectName){
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer == null) throw new IllegalArgumentException("Map layer not found: " + layerName);

        MapObject object = layer.getObjects().get(objectName);
        if (object == null) throw new IllegalArgumentException("Map object not found: " + objectName);

        Float x = object.getProperties().get("x", Float.class);
        Float y = object.getProperties().get("y", Float.class);

        if (x == null || y == null) throw new IllegalStateException("Map object has no valid position: " + objectName);

        return new Vector2(x, y);
    }

    public Rectangle getObjectRectangle(String layerName, String objectName){
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer == null)  throw new IllegalStateException("Layer not found: " + layerName);

        MapObject object = layer.getObjects().get(objectName);
        if (!(object instanceof RectangleMapObject rectangleMapObject)) throw new IllegalStateException("Rectangle object not found: " + objectName);

        return new Rectangle(rectangleMapObject.getRectangle());
    }

    public void setLayerVisible(String layerName, boolean visible){
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer == null) throw new IllegalStateException("Layer not found: " + layerName);
        layer.setVisible(visible);
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }
    public int getWidth() {return width;}
    public int getHeight() {return height;}

    public void dispose() {
        tiledMap.dispose();
    }
}















