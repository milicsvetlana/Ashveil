package com.ashveil.world.area;

import java.util.EnumMap;
import java.util.Map;

public class AreaRegistry {
    private final Map<AreaID, AreaDefinition> definitions;

    public AreaRegistry(){
        definitions = new EnumMap<>(AreaID.class);

        register(new AreaDefinition(AreaID.MAIN_ISLAND, "Main Island", "maps/main_island_v4.tmx"));
        register(new AreaDefinition(AreaID.WINDY_PLAINS, "Windy Plains", "maps/windy_plains.tmx"));
        register(new AreaDefinition(AreaID.DARKROOT_ISLE, "Darkroot Isle", "maps/darkroot_isle.tmx"));
        register(new AreaDefinition(AreaID.VEILSCAR_PASSAGE, "Veilscar Passage", "maps/veilscar_passage.tmx"));
    }

    private void register(AreaDefinition definition){
        definitions.put(definition.getId(), definition);
    }

    public AreaDefinition get(AreaID id){
        if (id == null) throw new IllegalArgumentException("Area id cannot be null.");

        AreaDefinition definition = definitions.get(id);
        if (definition == null) throw new IllegalArgumentException("Unknown area: " + id);

        return definition;
    }
}
