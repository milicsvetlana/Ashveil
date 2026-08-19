package com.ashveil.farming;

public class FarmingSystem {
    private final boolean[][] tilledTiles;
    private final Crop[][] crops;

    public FarmingSystem(int mapWidth, int mapHeight) {
        tilledTiles = new boolean[mapWidth][mapHeight];
        crops = new Crop[mapWidth][mapHeight];
    }

    public void update(float delta){
        for (int i=0; i < tilledTiles.length; i++){
            for (int j=0; j < tilledTiles[i].length; j++){
                if (crops[i][j] == null) continue;
                crops[i][j].updateAndCheckStageUpdate(delta);
            }
        }
    }

    public void plant(CropType cropType, int tileX, int tileY){
        crops[tileX][tileY] = new Crop(cropType);
    }

    public void till(int tileX, int tileY) {
        tilledTiles[tileX][tileY] = true;
    }
    public void removeCrop(int tileX, int tileY){
        crops[tileX][tileY] = null;
    }

    public Crop getCrop(int tileX, int tileY){return crops[tileX][tileY];}
    public boolean isTilled(int tileX, int tileY){return tilledTiles[tileX][tileY];}
}















