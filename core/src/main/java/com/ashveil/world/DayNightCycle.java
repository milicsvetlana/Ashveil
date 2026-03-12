package com.ashveil.world;

import com.ashveil.Config;

public class DayNightCycle {
    float dayTimer;
    int dayCount;
    boolean isNight;

    public DayNightCycle() {
        this.dayTimer = Config.DAY_DURATION;
        this.dayCount = 1;
        this.isNight = false;
    }

    public void update(float delta){
        dayTimer -= delta;
        if (dayTimer <= 0){
            isNight = !isNight;
            dayTimer = isNight ? Config.NIGHT_DURATION : Config.DAY_DURATION;
            if (!isNight) dayCount++;
        }
    }

    public float getDayTimer() {return dayTimer;}
    public int getDayCount() {return dayCount;}
    public boolean isNight() {return isNight;}
}
