package com.ashveil.save;

public class SaveSlotInfo {
    private final int slot;
    private final SaveSlotStatus status;
    private final long savedAt;
    private final double playTimeSeconds;
    private final String currentAreaId;
    private final int dayCount;

    private SaveSlotInfo(int slot, SaveSlotStatus status, long savedAt, double playTimeSeconds, String currentAreaId, int dayCount){
        this.slot = slot;
        this.status = status;
        this.savedAt = savedAt;
        this.playTimeSeconds = playTimeSeconds;
        this.currentAreaId = currentAreaId;
        this.dayCount = dayCount;
    }

    public static SaveSlotInfo empty(int slot){
        return new SaveSlotInfo(slot, SaveSlotStatus.EMPTY, 0L, 0.0, null, 0);
    }

    public static SaveSlotInfo invalid(int slot){
        return new SaveSlotInfo(slot, SaveSlotStatus.INVALID, 0L, 0.0, null, 0);
    }

    public static SaveSlotInfo valid(int slot, long savedAt, double playTimeSeconds, String currentAreaId, int dayCount){
        return new SaveSlotInfo(slot, SaveSlotStatus.VALID, savedAt, playTimeSeconds, currentAreaId, dayCount);
    }

    public int getSlot() {return slot;}
    public SaveSlotStatus getStatus() {return status;}
    public long getSavedAt() {return savedAt;}
    public double getPlayTimeSeconds() {return playTimeSeconds;}
    public String getCurrentAreaId() {return currentAreaId;}
    public int getDayCount() {return dayCount;}
}
