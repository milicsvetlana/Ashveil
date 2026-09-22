package com.ashveil.world;

import com.ashveil.world.area.AreaID;

//prakticno API, tj. skup metoda preko jedan deo programa sme komunicirati s drugim
public interface WorldMapAccess {
    AreaID getCurrentAreaId();
    boolean isAreaUnlocked(AreaID areaId);
    boolean isBoatTravelReady();
    boolean canAffordBoatTravel();
    int getBoatTravelCost();
    float getBoatTravelCooldownRemaining();
    int getGold();
    boolean travelToArea(AreaID destination);
}
