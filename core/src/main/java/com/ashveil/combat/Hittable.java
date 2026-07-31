package com.ashveil.combat;

public interface Hittable {
    float getCenterX();
    float getCenterY();

    void receiveHit(int amount);

    HitCategory getHitCategory();
}
