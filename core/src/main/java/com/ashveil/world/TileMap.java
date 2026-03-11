package com.ashveil.world;

import com.ashveil.Config;

public class TileMap {
    private TileType[][] tiles;

    public TileMap(){
        tiles = new TileType[Config.WORLD_HEIGHT][Config.WORLD_WIDTH];
        generate();
    }

    private void generate(){
        for (int y=0; y < Config.WORLD_HEIGHT; y++){
            for (int x=0; x < Config.WORLD_WIDTH; x++){
                if (x < 5 || x >= Config.WORLD_WIDTH - 5 ||
                    y < 5 || y >= Config.WORLD_HEIGHT - 5){
                    tiles[y][x] = TileType.WATER;
                } else if (x < 8 || x >= Config.WORLD_WIDTH - 8 ||
                    y < 8 || y >= Config.WORLD_HEIGHT - 8) {
                    tiles[y][x] = TileType.SAND;
                } else {
                    tiles[y][x] = TileType.GRASS;
                }
            }
        }
    }

    public TileType getTile(int x, int y){
        return tiles[y][x];
    }

    public boolean isBlocked(int x, int y){
        if (x < 0 || x >= Config.WORLD_WIDTH || y < 0 || y >= Config.WORLD_HEIGHT) return true;
        return tiles[y][x] == TileType.WATER;
    }

}
