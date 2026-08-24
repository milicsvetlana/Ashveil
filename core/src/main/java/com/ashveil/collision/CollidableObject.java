package com.ashveil.collision;

import com.ashveil.navigation.NavigationMode;
import com.badlogic.gdx.math.Rectangle;

public interface CollidableObject {
    Rectangle getCollisionBounds();
    boolean blocksMovement(MovementType movementType);
    boolean blocksNavigation(MovementType movementType, NavigationMode navigationMode);
}
