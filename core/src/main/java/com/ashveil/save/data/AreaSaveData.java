package com.ashveil.save.data;

import java.util.ArrayList;
import java.util.List;

public class AreaSaveData {
    public String areaId;
    public List<DestructibleObjectSaveData> destructibleObjects = new ArrayList<>();
    public List<TilledTileSaveData> tilledTiles = new ArrayList<>();
    public List<PlantSaveData> plants = new ArrayList<>();
    public List<WorldItemSaveData> groundItems = new ArrayList<>();
    public List<EnemySaveData> enemies = new ArrayList<>();
    public List<ProjectileSaveData> projectiles = new ArrayList<>();
    public NightSpawnSaveData nightSpawn;
}
