package com.ashveil.objects;

import com.ashveil.Config;
import com.ashveil.collision.CollidableObject;
import com.ashveil.collision.MovementType;
import com.ashveil.combat.HitCategory;
import com.ashveil.combat.Hittable;
import com.ashveil.navigation.NavigationMode;
import com.badlogic.gdx.math.Rectangle;

public class DestructibleObject extends WorldObject implements Hittable, CollidableObject {

    private final DestructibleObjectType type;
    private final Rectangle collisionBounds;

    public DestructibleObject(float x, float y, DestructibleObjectType type) {
        super(x, y, type.getHp());
        this.type = type;

        collisionBounds = new Rectangle(x, y, Config.TILE_SIZE, Config.TILE_SIZE);
    }

    public DestructibleObjectType getType() {return type;}

    @Override
    public boolean canReceiveHit() {return true;}

    @Override
    public void receiveHit(int amount) {hit(amount);}
    @Override
    public HitCategory getHitCategory() {return type.getHitCategory();}

    @Override
    public Rectangle getCollisionBounds() {
        return collisionBounds;
    }

    @Override
    public boolean blocksMovement(MovementType movementType) {
        return movementType == MovementType.GROUND;
    }

    @Override
    public boolean blocksNavigation(MovementType movementType, NavigationMode navigationMode) {
        if (movementType != MovementType.GROUND) return false;
        if (type == DestructibleObjectType.FENCE) return navigationMode == NavigationMode.NORMAL;
        return true;
    }
}
