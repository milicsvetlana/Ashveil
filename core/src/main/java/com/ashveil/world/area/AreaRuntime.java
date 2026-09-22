package com.ashveil.world.area;

import com.ashveil.collision.CollisionSystem;
import com.ashveil.combat.ProjectileSystem;
import com.ashveil.entities.Player;
import com.ashveil.entities.enemies.Enemy;
import com.ashveil.entities.enemies.EnemySpawnSystem;
import com.ashveil.farming.FarmingSystem;
import com.ashveil.navigation.DistanceField;
import com.ashveil.objects.DestructibleObjectSystem;
import com.ashveil.progression.ProgressionState;
import com.ashveil.world.TileMap;
import com.ashveil.world.WorldItemSystem;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

public class AreaRuntime implements Disposable {
    private final AreaID  areaID;
    private final TileMap tileMap;
    private final CollisionSystem collisionSystem;

    private final List<Enemy> enemies;

    private final WorldItemSystem worldItemSystem;
    private final DestructibleObjectSystem destructibleObjectSystem;
    private final FarmingSystem farmingSystem;

    private final DistanceField distanceField;
    private final ProjectileSystem projectileSystem;
    private final EnemySpawnSystem enemySpawnSystem;

    private int lastDepartureDayCount;
    private int ordinaryNightDayCount;

    public AreaRuntime(AreaDefinition definition, Player player, ProgressionState progressionState){
        if (definition == null) throw new IllegalArgumentException("Area definition cannot be null.");
        if (player == null) throw new IllegalArgumentException("Player cannot be null.");
        if (progressionState == null) throw new IllegalArgumentException("Progression state cannot be null.");

        areaID = definition.getId();

        tileMap = new TileMap(definition.getMapPath());
        collisionSystem = new CollisionSystem(tileMap);

        enemies = new ArrayList<>();
        worldItemSystem = new WorldItemSystem();

        farmingSystem = new FarmingSystem(tileMap.getWidth(), tileMap.getHeight());
        distanceField = new DistanceField(tileMap, collisionSystem);
        projectileSystem = new ProjectileSystem(player, collisionSystem);
        enemySpawnSystem = new EnemySpawnSystem(progressionState, player, tileMap, collisionSystem, distanceField, enemies, projectileSystem);
        destructibleObjectSystem = new DestructibleObjectSystem(tileMap, collisionSystem, progressionState, worldItemSystem);

        lastDepartureDayCount = -1;
        ordinaryNightDayCount = -1;
    }

    public void markDeparture(int dayCount){
        if (dayCount < 1) throw new IllegalArgumentException("Day count must be at least 1");
        lastDepartureDayCount = dayCount;
    }

    public void markOrdinaryNightStarted(int dayCount){
        if (dayCount < 1) throw new IllegalArgumentException("Day count must be at least 1");
        ordinaryNightDayCount = dayCount;
    }

    public void applyPersistentState(int lastDepartureDayCount, int ordinaryNightDayCount){
        if (lastDepartureDayCount < -1) throw new IllegalArgumentException("Last departure day count cannot be less than -1.");
        if (ordinaryNightDayCount < -1) throw new IllegalArgumentException("Ordinary night day count cannot be less than -1.");

        this.lastDepartureDayCount = lastDepartureDayCount;
        this.ordinaryNightDayCount = ordinaryNightDayCount;
    }

    public AreaID getAreaID() {return areaID;}
    public TileMap getTileMap() {return tileMap;}
    public CollisionSystem getCollisionSystem() {return collisionSystem;}
    public List<Enemy> getEnemies() {return enemies;}
    public WorldItemSystem getWorldItemSystem() {return worldItemSystem;}
    public DestructibleObjectSystem getDestructibleObjectSystem() {return destructibleObjectSystem;}
    public FarmingSystem getFarmingSystem() {return farmingSystem;}
    public DistanceField getDistanceField() {return distanceField;}
    public ProjectileSystem getProjectileSystem() {return projectileSystem;}
    public EnemySpawnSystem getEnemySpawnSystem() {return enemySpawnSystem;}
    public int getLastDepartureDayCount() {return lastDepartureDayCount;}
    public int getOrdinaryNightDayCount() {return ordinaryNightDayCount;}

    @Override
    public void dispose() {
        tileMap.dispose();
    }
}
