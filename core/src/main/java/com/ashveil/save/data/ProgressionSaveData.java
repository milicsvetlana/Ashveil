package com.ashveil.save.data;

import com.ashveil.world.area.AreaID;

import java.util.ArrayList;
import java.util.List;

public class ProgressionSaveData {
    public boolean firstTreeDropClaimed;
    public boolean wispNightUnlocked;
    public boolean wraithNightUnlocked;

    public boolean boatKitCrafted;
    public boolean boatBuilt;
    public boolean foundOldJetty;

    public boolean scrollIRead;
    public boolean scrollIIRead;
    public boolean scrollIIIRead;

    public List<String> unlockedCraftingCategories = new ArrayList<>();
    public List<String> unlockedAreas = new ArrayList<>();

    public boolean windyWardCleared;
    public boolean dashUnlocked;

    public boolean darkrootWardCleared;
    public boolean veilscarWardCleared;
}
