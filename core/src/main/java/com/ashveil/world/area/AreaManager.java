package com.ashveil.world.area;

public class AreaManager {
    private final AreaRegistry areaRegistry;
    private AreaID currentAreaId;

    public AreaManager(){
        this(AreaID.MAIN_ISLAND);
    }

    public AreaManager(AreaID initialAreaID){
        areaRegistry = new AreaRegistry();
        setCurrentArea(initialAreaID);
    }

    public AreaID getCurrentAreaId(){return currentAreaId;}
    public AreaDefinition getCurrentArea(){
        return areaRegistry.get(currentAreaId);
    }
    public AreaDefinition getArea(AreaID id){
        return areaRegistry.get(id);
    }

    public void setCurrentArea(AreaID areaId) {
        if (areaId == null) throw new IllegalArgumentException("Area id cannot be null.");
        this.currentAreaId = areaId;
    }
}
