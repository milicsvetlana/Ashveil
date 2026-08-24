package com.ashveil.navigation;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.collision.MovementType;
import com.ashveil.world.TileMap;

import java.util.ArrayDeque;
import java.util.Queue;

public class DistanceField {
    private final TileMap tileMap;
    private final CollisionSystem collisionSystem;
    private final int[][] normalDistances;
    private final int[][] breakFenceDistances;

    private int targetTileX;
    private int targetTileY;
    private long lastRevisionUpdate;

    public static final int UNREACHABLE = -1;
    private enum BfsState {
        WHITE,
        GREY,
        BLACK
    }
    private final BfsState[][] states;
    private final Queue<int[]> queue;
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public DistanceField(TileMap tileMap, CollisionSystem collisionSystem){
        this.tileMap = tileMap;
        this.collisionSystem = collisionSystem;
        normalDistances = new int[tileMap.getWidth()][tileMap.getHeight()];
        breakFenceDistances = new int[tileMap.getWidth()][tileMap.getHeight()];
        states = new BfsState[tileMap.getWidth()][tileMap.getHeight()];
        lastRevisionUpdate = -1;
        targetTileX = -1;
        targetTileY = -1;
        queue = new ArrayDeque<>();
    }

    public void update(int targetTileX, int targetTileY){
        if (targetTileX < 0 || targetTileX >= tileMap.getWidth() || targetTileY < 0 || targetTileY >= tileMap.getHeight()) return;
        boolean targetChanged = this.targetTileX != targetTileX || this.targetTileY != targetTileY;
        boolean collisionChanged = lastRevisionUpdate != collisionSystem.getRevision();
        if (!targetChanged && !collisionChanged) return;

        lastRevisionUpdate = collisionSystem.getRevision();
        this.targetTileX = targetTileX;
        this.targetTileY = targetTileY;
        bfsUpdate(normalDistances, NavigationMode.NORMAL);
        bfsUpdate(breakFenceDistances, NavigationMode.BREAK_FENCES);
    }

    private void bfsUpdate(int[][] distances, NavigationMode navigationMode){
        resetBfs(distances);

        while (!queue.isEmpty()){
            int[] current = queue.remove();

            int currentX = current[0];
            int currentY = current[1];

            for (int[] direction : DIRECTIONS){
                int neigX = currentX + direction[0];
                int neigY = currentY + direction[1];

                if (neigX < 0 || neigX >= tileMap.getWidth() || neigY < 0 || neigY >= tileMap.getHeight()) continue;
                if (states[neigX][neigY] != BfsState.WHITE) continue;

                float worldX = tileMap.tileToWorldX(neigX);
                float worldY = tileMap.tileToWorldY(neigY);

                if (collisionSystem.isNavigationBlocked(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE,
                    MovementType.GROUND, navigationMode)) continue;

                states[neigX][neigY] = BfsState.GREY;
                distances[neigX][neigY] = distances[currentX][currentY] + 1;
                queue.add(new int[]{neigX, neigY});
            }

            states[currentX][currentY] = BfsState.BLACK;
        }
    }

    private void resetBfs(int[][] distances){
        for (int i=0; i < tileMap.getWidth(); i++){
            for (int j=0; j < tileMap.getHeight(); j++){
                distances[i][j] = UNREACHABLE;
                states[i][j] = BfsState.WHITE;
            }
        }
        distances[targetTileX][targetTileY] = 0;
        states[targetTileX][targetTileY] = BfsState.GREY;
        queue.clear();
        queue.add(new int[]{targetTileX, targetTileY});
    }

    public boolean isNavigationBlocked(int tileX, int tileY, NavigationMode navigationMode){
        if (tileX < 0 || tileX >= tileMap.getWidth() || tileY < 0 || tileY >= tileMap.getHeight()) return true;
        float worldX = tileMap.tileToWorldX(tileX);
        float worldY = tileMap.tileToWorldY(tileY);
        return collisionSystem.isNavigationBlocked(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE, MovementType.GROUND, navigationMode);
    }

    public int getDistance(int tileX, int tileY, NavigationMode navigationMode){
        if (tileX < 0 || tileX >= tileMap.getWidth() || tileY < 0 || tileY >= tileMap.getHeight()) return UNREACHABLE;
        if (navigationMode == NavigationMode.NORMAL)return normalDistances[tileX][tileY];
        return breakFenceDistances[tileX][tileY];
    }

    public int worldToTileX(float worldX){
        return tileMap.worldToTileX(worldX);
    }
    public int worldToTileY(float worldY){
        return tileMap.worldToTileY(worldY);
    }
    public float tileToWorldX(int tileX){
        return tileMap.tileToWorldX(tileX);
    }
    public float tileToWorldY(int tileY){
        return tileMap.tileToWorldY(tileY);
    }
}













