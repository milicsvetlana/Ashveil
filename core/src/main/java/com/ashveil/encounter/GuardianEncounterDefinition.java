package com.ashveil.encounter;

import com.ashveil.entities.enemies.EnemyType;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.world.area.AreaID;

import java.util.Map;

public class GuardianEncounterDefinition {
    private final AreaID areaID;
    private final ItemType scrollItem;
    private final Map<EnemyType, Integer> waveComposition;

    public GuardianEncounterDefinition(AreaID areaID, ItemType scrollItem, Map<EnemyType, Integer> waveComposition){
        if (areaID == null) throw new IllegalArgumentException("Area ID cannot be null.");
        if (scrollItem == null) throw new IllegalArgumentException("Scroll item cannot be null.");
        if (waveComposition == null) throw new IllegalArgumentException("Wave composition cannot be null.");

        this.areaID = areaID;
        this.scrollItem = scrollItem;
        this.waveComposition = Map.copyOf(waveComposition);
    }

    public AreaID getAreaID() {return areaID;}
    public ItemType getScrollItem() {return scrollItem;}
    public Map<EnemyType, Integer> getWaveComposition() {return waveComposition;}
}
