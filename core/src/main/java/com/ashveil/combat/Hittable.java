package com.ashveil.combat;

public interface Hittable {
    float getCenterX();
    float getCenterY();

    boolean canReceiveHit();
    void receiveHit(int amount);

    HitCategory getHitCategory();
}
