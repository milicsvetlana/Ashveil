package com.ashveil.farming;

public class Crop {
    private CropStage cropStage;
    private float growthTimer;
    private CropType cropType;

    public Crop(CropType cropType) {
        this.cropType = cropType;
        cropStage = CropStage.SEED;
        growthTimer = 0;
    }

    public void updateAndCheckStageUpdate(float delta){ //apdejtuje tajmer i, ako treba, prelazi u novi crop stage
        growthTimer += delta;
        CropStage newStage;

        if (growthTimer < cropType.getSeedDuration()) newStage = CropStage.SEED;
        else if (growthTimer < cropType.getSeedDuration() + cropType.getSproutDuration()) newStage = CropStage.SPROUT;
        else if (growthTimer < cropType.getSeedDuration() + cropType.getSproutDuration() + cropType.getGrowingDuration()) newStage = CropStage.GROWING;
        else newStage = CropStage.MATURE;

        if (newStage != cropStage) cropStage = newStage;
    }

    public CropStage getCropStage() {return cropStage;}
}
