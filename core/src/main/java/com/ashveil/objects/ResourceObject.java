package com.ashveil.objects;

import com.ashveil.Config;
import com.ashveil.collision.CollidableObject;
import com.ashveil.combat.HitCategory;
import com.ashveil.combat.Hittable;
import com.badlogic.gdx.math.Rectangle;

public class ResourceObject extends WorldObject implements Hittable, CollidableObject {

    private final ResourceType type;
    private final Rectangle collisionBounds;

    public ResourceObject(float x, float y, ResourceType type) {
        super(x, y, type.getHp());
        this.type = type;

        collisionBounds = new Rectangle(x, y, Config.TILE_SIZE, Config.TILE_SIZE);
    }

    public ResourceType getType() {return type;}

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
}
