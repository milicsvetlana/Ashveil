package com.ashveil.save.data;

import com.ashveil.progression.ProgressionState;

public class SaveData {
    public int saveVersion;
    public long savedAt;
    public double playTimeSeconds;

    public PlayerSaveData player;
    public DayNightSaveData dayNight;
    public ProgressionSaveData progressionState;
}
