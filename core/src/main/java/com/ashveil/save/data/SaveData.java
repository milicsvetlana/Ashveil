package com.ashveil.save.data;

import com.ashveil.progression.ProgressionState;

import java.util.ArrayList;
import java.util.List;

public class SaveData {
    public int saveVersion;
    public long savedAt;
    public double playTimeSeconds;

    public PlayerSaveData player;
    public DayNightSaveData dayNight;
    public ProgressionSaveData progressionState;

    public String currentAreaId;
    public List<AreaSaveData> areas = new ArrayList<>();
}
