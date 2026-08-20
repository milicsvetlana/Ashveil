package com.ashveil.farming;

import com.ashveil.Config;

public class Sapling extends GrowablePlant {

    public Sapling(){
        super(Config.SAPLING_EARLY_DURATION, Config.SAPLING_MIDDLE_DURATION, Config.SAPLING_LATE_DURATION);
    }
}
