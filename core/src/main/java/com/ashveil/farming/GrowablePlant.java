package com.ashveil.farming;

public class GrowablePlant {
    private GrowthStage growthStage;
    private float growthTimer;
    private final float earlyDuration;
    private final float middleDuration;
    private final float lateDuration;

    public GrowablePlant(float earlyDuration, float middleDuration, float lateDuration){
        this.earlyDuration = earlyDuration;
        this.middleDuration = middleDuration;
        this.lateDuration = lateDuration;
        growthStage = GrowthStage.EARLY;
        growthTimer = 0;
    }

    public void update(float delta){ //apdejtuje tajmer i, ako treba, prelazi u novi crop stage
        growthTimer += delta;
        updateGrowthStage();
    }

    public void updateGrowthStage(){
        GrowthStage newStage;

        if (growthTimer < earlyDuration) newStage = GrowthStage.EARLY;
        else if (growthTimer < earlyDuration + middleDuration) newStage = GrowthStage.MIDDLE;
        else if (growthTimer < earlyDuration + middleDuration + lateDuration) newStage = GrowthStage.LATE;
        else newStage = GrowthStage.MATURE;
        if (newStage != growthStage) growthStage = newStage;
    }

    public void restoreGrowthTimer(float growthTimer){
        this.growthTimer = growthTimer;
        updateGrowthStage();
    }

    public GrowthStage getGrowthStage() {return growthStage;}
    public float getGrowthTimer(){return growthTimer;}
}
