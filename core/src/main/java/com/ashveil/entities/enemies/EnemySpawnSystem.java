package com.ashveil.entities.enemies;

import com.ashveil.Config;
import com.ashveil.collision.CollisionSystem;
import com.ashveil.collision.MovementType;
import com.ashveil.combat.ProjectileSystem;
import com.ashveil.entities.Player;
import com.ashveil.navigation.DistanceField;
import com.ashveil.navigation.NavigationMode;
import com.ashveil.progression.ProgressionState;
import com.ashveil.world.TileMap;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class EnemySpawnSystem {
    private final ProgressionState progressionState;
    private final Queue<EnemyType> spawnQueue;
    private float spawnTimer;
    private float spawnInterval;
    private final Random random = new Random();

    private final Player player;
    private final TileMap tileMap;
    private final CollisionSystem collisionSystem;
    private final DistanceField distanceField;
    private final List<Enemy> enemies;
    private final ProjectileSystem projectileSystem;

    private final Rectangle spawnBounds;

    public EnemySpawnSystem(ProgressionState progressionState, Player player, TileMap tileMap,
                            CollisionSystem collisionSystem, DistanceField distanceField, List<Enemy> enemies,
                            ProjectileSystem projectileSystem){
        this.progressionState = progressionState;
        this.player = player;
        this.tileMap = tileMap;
        this.collisionSystem = collisionSystem;
        this.distanceField = distanceField;
        this.enemies = enemies;
        this.projectileSystem = projectileSystem;

        this.spawnQueue = new ArrayDeque<>();
        this.spawnBounds = new Rectangle();

        this.spawnTimer = 0;
        this.spawnInterval = 0;
    }

    public void update(float delta){
        if (spawnQueue.isEmpty()) return;
        spawnTimer += delta;
        if (spawnTimer < spawnInterval) return;
        spawnTimer -= spawnInterval;
        EnemyType enemyType = spawnQueue.element();
        if (trySpawningEnemy(enemyType)) spawnQueue.remove();
    }

    public void startNight(int dayCount) {
        spawnQueue.clear();
        int remainingBudget = Config.INITIAL_NIGHT_THREAT_BUDGET + (dayCount - 1) * Config.NIGHT_THREAT_BUDGET_INCREASE;

        List<EnemyType> unlockedTypes = new ArrayList<>();
        unlockedTypes.add(EnemyType.SHADE);

        if (progressionState.isWispNightUnlocked()) unlockedTypes.add(EnemyType.WISP);
        if (progressionState.isWraithNightUnlocked()) unlockedTypes.add(EnemyType.WRAITH);

        while (remainingBudget > 0) {
            List<EnemyType> affordableTypes = new ArrayList<>();

            for (EnemyType enemyType : unlockedTypes) {
                if (enemyType.getThreatCost() <= remainingBudget) affordableTypes.add(enemyType);
            }
            if (affordableTypes.isEmpty()) break;

            EnemyType selectedType = affordableTypes.get(random.nextInt(affordableTypes.size()));
            spawnQueue.add(selectedType);
            remainingBudget -= selectedType.getThreatCost();
        }

        spawnTimer = 0;
        //Config.nightduration * 0.7 prakticno predstavlja prostor kad moze da se spawna
        if (!spawnQueue.isEmpty()) spawnInterval = Config.NIGHT_DURATION * 0.7f / spawnQueue.size();
    }

    private int[] findSpawnTile(EnemyType enemyType){
        spawnInterval = 0;
        int playerTileX = tileMap.worldToTileX(player.getCenterX());
        int playerTileY = tileMap.worldToTileY(player.getCenterY());

        for (int attempt = 0; attempt < Config.ENEMY_SPAWN_MAX_ATTEMPTS; attempt++){
            int tileX = playerTileX + random.nextInt(Config.ENEMY_SPAWN_MAX_TILE_DISTANCE * 2 + 1) - Config.ENEMY_SPAWN_MAX_TILE_DISTANCE;
            int tileY = playerTileY + random.nextInt(Config.ENEMY_SPAWN_MAX_TILE_DISTANCE * 2 + 1) - Config.ENEMY_SPAWN_MAX_TILE_DISTANCE;

            if (tileMap.isOutOfBounds(tileX, tileY)) continue;

            int distanceX = tileX - playerTileX;
            int distanceY = tileY - playerTileY;
            int distanceSquared = distanceX * distanceX + distanceY * distanceY;
            if (distanceSquared < Config.ENEMY_SPAWN_MIN_TILE_DISTANCE * Config.ENEMY_SPAWN_MIN_TILE_DISTANCE
                || distanceSquared > Config.ENEMY_SPAWN_MAX_TILE_DISTANCE * Config.ENEMY_SPAWN_MAX_TILE_DISTANCE
            ) continue;

            float worldX = tileMap.tileToWorldX(tileX);
            float worldY = tileMap.tileToWorldY(tileY);
            spawnBounds.set(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE);

            boolean overlapsEnemy = false;
            for (Enemy enemy : enemies){
                if (spawnBounds.overlaps(enemy.getCollisionBounds())){
                    overlapsEnemy = true;
                    break;
                }
            }
            if (overlapsEnemy) continue;

            if (enemyType.getMovementType() == MovementType.GROUND){
                if (collisionSystem.isBlocked(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE, MovementType.GROUND)) continue;
                NavigationMode navigationMode = enemyType == EnemyType.SHADE ? NavigationMode.BREAK_FENCES : NavigationMode.NORMAL;
                if (distanceField.getDistance(tileX, tileY, navigationMode) == DistanceField.UNREACHABLE) continue;
            }
            else{
                if (collisionSystem.getBlockingObject(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE, MovementType.GROUND) != null) continue;
            }
            return new int[]{tileX, tileY};
        }
        return null;
    }

    private Enemy createEnemy(EnemyType enemyType, float worldX, float worldY){
        return switch (enemyType){
            case SHADE ->
                new Shade(worldX, worldY, player, collisionSystem, distanceField);
            case WISP ->
                new Wisp(worldX, worldY, player, collisionSystem);

            case WRAITH ->
                new Wraith(worldX, worldY, player, collisionSystem, distanceField, projectileSystem);
        };
    }

    private boolean trySpawningEnemy(EnemyType enemyType){
        int[] spawnTile = findSpawnTile(enemyType);
        if (spawnTile == null) return false;

        float worldX = tileMap.tileToWorldX(spawnTile[0]);
        float worldY = tileMap.tileToWorldY(spawnTile[1]);

        enemies.add(createEnemy(enemyType, worldX, worldY));
        return true;
    }

    public void endNight(){
        spawnQueue.clear();
        spawnTimer = 0;
        spawnInterval = 0;
    }

}
