package com.ashveil.objects;

import com.ashveil.collision.MovementType;
import com.ashveil.navigation.NavigationMode;

public class Hollowcap extends DestructibleObject{
    public Hollowcap(float x, float y, int currentHp){
        super(x, y, DestructibleObjectType.HOLLOWCAP, currentHp);
    }

    @Override
    public boolean blocksMovement(MovementType movementType) {
        return false;
    }

    @Override
    public boolean blocksNavigation(MovementType movementType, NavigationMode navigationMode){
        return false;
    }
}
