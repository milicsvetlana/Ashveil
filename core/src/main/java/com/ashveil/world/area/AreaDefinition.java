package com.ashveil.world.area;

public class AreaDefinition {
    private final AreaID id;
    private final String displayName;
    private final String mapPath;

    public AreaDefinition(AreaID id, String displayName, String mapPath){
        if (id == null) throw new IllegalArgumentException("Area ID cannot be null.");
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Name cannot be null.");
        if (mapPath == null) throw new IllegalArgumentException("Map Path cannot be null.");

        this.id = id;
        this.displayName = displayName;
        this.mapPath = mapPath;
    }

    public AreaID getId() {return id;}
    public String getDisplayName() {return displayName;}
    public String getMapPath() {return mapPath;}
}
