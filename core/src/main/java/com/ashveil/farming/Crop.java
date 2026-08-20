package com.ashveil.farming;

public class Crop extends GrowablePlant {
    private CropType cropType;

    public Crop(CropType cropType) {
        super(cropType.getEarlyDuration(), cropType.getMiddleDuration(), cropType.getLateDuration());
        this.cropType = cropType;
    }
}
