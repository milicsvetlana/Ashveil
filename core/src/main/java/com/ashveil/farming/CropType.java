package com.ashveil.farming;

import com.ashveil.Config;

public enum CropType {
    WHEAT (Config.WHEAT_EARLY_DURATION, Config.WHEAT_MIDDLE_DURATION, Config.WHEAT_LATE_DURATION),
    //specijalna biljka
    ;

    private final float earlyDuration;
    private final float middleDuration;
    private final float lateDuration;

    CropType(float earlyDuration, float middleDuration, float lateDuration){
        this.earlyDuration = earlyDuration;
        this.middleDuration = middleDuration;
        this.lateDuration = lateDuration;
    }

    public float getEarlyDuration() {return earlyDuration;}
    public float getMiddleDuration() {return middleDuration;}
    public float getLateDuration() {return lateDuration;}
}
