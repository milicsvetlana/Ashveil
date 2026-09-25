package com.ashveil.objects;

import com.ashveil.Config;
import com.ashveil.collision.MovementType;
import com.ashveil.navigation.NavigationMode;

public class BriarSnare extends DestructibleObject{
    public BriarSnare(float x, float y){
        super(x, y, DestructibleObjectType.BRIAR_SNARE, Config.BRIAR_SNARE_HP);
    }

    @Override
    public boolean blocksMovement(MovementType movementType){
        return false;
    }

    public boolean blocksNavigation(MovementType movementType, NavigationMode navigationMode) {
        return false;
    }

    public void trigger(){
        receiveHit(getCurrenthp());
    }
}
