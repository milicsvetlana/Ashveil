package com.ashveil.world;

import com.ashveil.Config;

public class DayNightCycle {
    private float dayTimer;
    private int dayCount;
    private boolean isNight;
    private boolean justBecameNight;

    public DayNightCycle(){
        this.dayTimer = Config.DAY_DURATION;
        this.dayCount = 1;
        this.isNight = false;
        this.justBecameNight = false;
    }

    public void update(float delta){
        boolean wasNight = isNight;
        dayTimer -= delta;
        if (dayTimer <= 0){
            isNight = !isNight;
            dayTimer = isNight ? Config.NIGHT_DURATION : Config.DAY_DURATION;
            if (!isNight) dayCount++;
        }
        justBecameNight = !wasNight && isNight;
    }

    public float getDayTimer() {return dayTimer;}
    public int getDayCount() {return dayCount;}
    public boolean isNight() {return isNight;}
    public boolean justBecameNight() {return justBecameNight;}
}
