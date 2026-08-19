package com.ashveil.farming;

import com.ashveil.Config;

public enum CropType {
    WHEAT (Config.WHEAT_SEED_DURATION, Config.WHEAT_SPROUT_DURATION, Config.WHEAT_GROWING_DURATION),
    //specijalna biljka
    ;

    private final float seedDuration;
    private final float sproutDuration;
    private final float growingDuration;

    CropType(float seedDuration, float sproutDuration, float growingDuration){
        this.seedDuration = seedDuration;
        this.sproutDuration = sproutDuration;
        this.growingDuration = growingDuration;
    }

    public float getSeedDuration() {return seedDuration;}
    public float getSproutDuration() {return sproutDuration;}
    public float getGrowingDuration() {return growingDuration;}
}
