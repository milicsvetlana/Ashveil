package com.ashveil.objects;

import com.ashveil.combat.HitCategory;
import com.ashveil.combat.Hittable;

public class ResourceObject extends WorldObject implements Hittable {

    private final ResourceType type;

    public ResourceObject(float x, float y, ResourceType type) {
        super(x, y, type.getHp());
        this.type = type;
    }

    public ResourceType getType() {return type;}

    @Override
    public void receiveHit(int amount) {hit(amount);}
    @Override
    public HitCategory getHitCategory() {return type.getHitCategory();}
}
