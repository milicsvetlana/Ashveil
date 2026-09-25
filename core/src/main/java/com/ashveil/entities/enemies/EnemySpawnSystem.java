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
import com.ashveil.world.area.AreaID;
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

    public void startNight(int dayCount, AreaID areaID) {
        if (areaID == null) throw new IllegalArgumentException("Area ID cannot be null");

        spawnQueue.clear();
        int remainingBudget = Config.INITIAL_NIGHT_THREAT_BUDGET + (dayCount - 1) * Config.NIGHT_THREAT_BUDGET_INCREASE;

        List<EnemyType> availableTypes = getAvailableEnemyTypes(areaID);

        while (remainingBudget > 0) {
            List<EnemyType> affordableTypes = new ArrayList<>();

            for (EnemyType enemyType : availableTypes) {
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

    private List<EnemyType> getAvailableEnemyTypes(AreaID areaID){
        List <EnemyType> available = new ArrayList<>();
        available.add(EnemyType.SHADE);

        switch (areaID) {
            case MAIN_ISLAND -> {
                if (progressionState.isWispNightUnlocked()) {available.add(EnemyType.WISP);}
                if (progressionState.isWraithNightUnlocked()) {available.add(EnemyType.WRAITH);}
            }
            case WINDY_PLAINS -> {
                available.add(EnemyType.WISP);
                if (progressionState.isWraithNightUnlocked()) {available.add(EnemyType.WRAITH);}
            }

            case DARKROOT_ISLE, VEILSCAR_PASSAGE -> {
                available.add(EnemyType.WISP);
                available.add(EnemyType.WRAITH);
            }
        }
        return available;
    }

    private int[] findSpawnTile(EnemyType enemyType){
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
                if (tileMap.isHazard(tileX, tileY)) continue;
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

    private Enemy createEnemy(EnemyType enemyType, float worldX, float worldY, int currentHp){
        return switch (enemyType){
            case SHADE ->
                new Shade(worldX, worldY, player, collisionSystem, distanceField, currentHp);
            case WISP ->
                new Wisp(worldX, worldY, player, collisionSystem, currentHp);

            case WRAITH ->
                new Wraith(worldX, worldY, player, collisionSystem, distanceField, projectileSystem, currentHp);
        };
    }

    private boolean trySpawningEnemy(EnemyType enemyType){
        int[] spawnTile = findSpawnTile(enemyType);
        if (spawnTile == null) return false;

        float worldX = tileMap.tileToWorldX(spawnTile[0]);
        float worldY = tileMap.tileToWorldY(spawnTile[1]);

        createAndAddEnemy(enemyType, worldX, worldY);
        return true;
    }

    public void endNight(){
        spawnQueue.clear();
        spawnTimer = 0;
        spawnInterval = 0;
    }

    public Enemy createAndAddEnemy(EnemyType enemyType, float worldX, float worldY){
        return createAndAddEnemy(enemyType, worldX, worldY, enemyType.getMaxHp());
    }

    public Enemy createAndAddEnemy(EnemyType enemyType, float worldX, float worldY, int currentHp){
        Enemy enemy = createEnemy(enemyType, worldX, worldY, currentHp);
        enemies.add(enemy);
        return enemy;
    }

    public void applyPersistentState(List<EnemyType> remainingQueue, float spawnTimer, float spawnInterval){
        if (remainingQueue == null) throw new IllegalArgumentException("Remaining spawn queue cannot be null.");
        if (Float.isNaN(spawnTimer) || Float.isInfinite(spawnTimer) || spawnTimer < 0)
            throw new IllegalArgumentException ("Invalid spawn timer.");

        if (Float.isNaN(spawnInterval) || Float.isInfinite(spawnInterval) || spawnInterval < 0)
            throw new IllegalArgumentException("Invalid spawn interval.");

        if (!remainingQueue.isEmpty() && spawnInterval <= 0) throw new IllegalArgumentException("Non-empty spawn" +
            " queue requires a positive spawn interval.");

        for (EnemyType enemyType : remainingQueue){
            if (enemyType == null) throw new IllegalArgumentException("Spawn queue cannot contain null enemy types.");
        }

        this.spawnQueue.clear();
        this.spawnQueue.addAll(remainingQueue);
        this.spawnTimer = spawnTimer;
        this.spawnInterval = spawnInterval;
    }

    public Enemy spawnEnemyInRegion(EnemyType enemyType, Rectangle region){
        if (enemyType == null) throw new IllegalArgumentException("Enemy type cannot be null.");
        if (region == null) throw new IllegalArgumentException("Spawn region cannot be null.");

        for (int attempt = 0; attempt < Config.ENEMY_SPAWN_MAX_ATTEMPTS; attempt++){
            float randomX = region.x + random.nextFloat() * Math.max(0f, region.width - Config.TILE_SIZE);
            float randomY = region.y + random.nextFloat() * Math.max(0f, region.height - Config.TILE_SIZE);

            int tileX = tileMap.worldToTileX(randomX);
            int tileY = tileMap.worldToTileY(randomY);
            if (tileMap.isOutOfBounds(tileX, tileY)) continue;

            float worldX = tileMap.tileToWorldX(tileX);
            float worldY = tileMap.tileToWorldY(tileY);

            spawnBounds.set(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE);
            if (!region.contains(spawnBounds)) continue;


            boolean overlapsEnemy = false;
            for (Enemy enemy : enemies){
                if (spawnBounds.overlaps(enemy.getCollisionBounds())){
                    overlapsEnemy = true;
                    break;
                }
            }
            if (overlapsEnemy) continue;

            if (spawnBounds.overlaps(player.getCollisionBounds())) continue;

            if (enemyType.getMovementType() == MovementType.GROUND){
                if (tileMap.isHazard(tileX, tileY)) continue;
                if (collisionSystem.isBlocked(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE, MovementType.GROUND)) continue;
                NavigationMode navigationMode = enemyType == EnemyType.SHADE ? NavigationMode.BREAK_FENCES : NavigationMode.NORMAL;
                if (distanceField.getDistance(tileX, tileY, navigationMode) == DistanceField.UNREACHABLE) continue;
            }
            else{
                if (collisionSystem.getBlockingObject(worldX, worldY, Config.TILE_SIZE, Config.TILE_SIZE,
                    MovementType.GROUND) != null) continue;
            }

            return createAndAddEnemy(enemyType, worldX, worldY);
        }
        return null;
    }

    //saljemo novi ArrayList zato sto zelimo zabraniti da neko spolja dobije stvarni queue.
    public List<EnemyType> getRemainingSpawnQueue(){return new ArrayList<>(spawnQueue);}
    public float getSpawnTimer(){return spawnTimer;}
    public float getSpawnInterval(){return spawnInterval;}
}
