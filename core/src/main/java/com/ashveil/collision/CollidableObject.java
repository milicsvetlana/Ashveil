package com.ashveil.collision;

import com.badlogic.gdx.math.Rectangle;

public interface CollidableObject {
    Rectangle getCollisionBounds();
    boolean blocksMovement(MovementType movementType);
}
