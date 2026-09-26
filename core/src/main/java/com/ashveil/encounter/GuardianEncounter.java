package com.ashveil.encounter;

import com.ashveil.entities.enemies.EnemyType;
import com.ashveil.world.area.AreaID;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;

import java.util.EnumMap;
import java.util.Map;


public class GuardianEncounter {
    private final AreaID areaID;
    private final Rectangle arenaBounds;
    private final Map<EnemyType, Integer> waveComposition;

    private GuardianEncounterState state;

    public GuardianEncounter(AreaID areaID, Rectangle arenaBounds, Map<EnemyType, Integer> waveComposition, boolean alreadyCleared){
        if (areaID == null) throw new IllegalArgumentException("Area ID cannot be null.");
        if (arenaBounds == null) throw new IllegalArgumentException("Arena bounds cannot be null.");
        if (waveComposition == null || waveComposition.isEmpty()) throw new IllegalArgumentException("Wave composition cannot be empty.");

        this.areaID = areaID;
        this.arenaBounds = new Rectangle(arenaBounds);

        EnumMap<EnemyType, Integer> composition = new EnumMap<>(EnemyType.class);

        for (Map.Entry<EnemyType, Integer> entry : waveComposition.entrySet()){
            if (entry.getKey() == null) throw new IllegalArgumentException("Enemy type cannot be null.");
            if (entry.getValue() == null || entry.getValue() <= 0) throw new IllegalArgumentException("Enemy amount must be positive.");
            composition.put(entry.getKey(), entry.getValue());
        }

        this.waveComposition = Map.copyOf(composition);

        state = alreadyCleared ? GuardianEncounterState.CLEARED : GuardianEncounterState.INACTIVE;
    }

    public boolean start(){
        if (state != GuardianEncounterState.INACTIVE) return false;
        state = GuardianEncounterState.ACTIVE;
        return true;
    }

    public boolean complete(){
        if (state != GuardianEncounterState.ACTIVE) return false;
        state = GuardianEncounterState.CLEARED;
        return true;
    }

    public void reset(){
        if (state == GuardianEncounterState.CLEARED) return;
        state = GuardianEncounterState.INACTIVE;
    }

    public boolean isInactive(){
        return state == GuardianEncounterState.INACTIVE;
    }
    public boolean isActive(){
        return state == GuardianEncounterState.ACTIVE;
    }
    public boolean isCleared(){
        return state == GuardianEncounterState.CLEARED;
    }

    public AreaID getAreaID(){return areaID;}
    public Rectangle getArenaBounds(){return new Rectangle(arenaBounds);}
    public Map<EnemyType, Integer> getWaveComposition(){return waveComposition;}
    public GuardianEncounterState getState(){return state;}
}
