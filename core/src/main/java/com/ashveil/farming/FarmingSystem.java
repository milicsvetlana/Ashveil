package com.ashveil.farming;

public class FarmingSystem {
    private final boolean[][] tilledTiles;
    private final GrowablePlant[][] plants;

    public FarmingSystem(int mapWidth, int mapHeight) {
        tilledTiles = new boolean[mapWidth][mapHeight];
        plants = new GrowablePlant[mapWidth][mapHeight];
    }

    public void update(float delta){
        for (int i=0; i < tilledTiles.length; i++){
            for (int j=0; j < tilledTiles[i].length; j++){
                if (plants[i][j] == null) continue;
                plants[i][j].update(delta);
            }
        }
    }

    public void plant(CropType cropType, int tileX, int tileY){
        plants[tileX][tileY] = new Crop(cropType);
    }
    public void plant(String string, int tileX, int tileY){plants[tileX][tileY] = new Sapling();}

    public void till(int tileX, int tileY) {
        tilledTiles[tileX][tileY] = true;
    }
    public void removePlant(int tileX, int tileY){
        plants[tileX][tileY] = null;
    }

    public GrowablePlant getPlant(int tileX, int tileY){return plants[tileX][tileY];}
    public boolean isTilled(int tileX, int tileY){return tilledTiles[tileX][tileY];}

    public int getWidth(){return tilledTiles.length;}
    public int getHeight(){return tilledTiles[0].length;}

}















