package com.ashveil.save.data;

import java.util.ArrayList;
import java.util.List;

public class PlayerSaveData {
    public float x;
    public float y;
    public float checkPointX;
    public float checkPointY;
    public int health;
    public int brokenHearts;
    public int gold;
    public int selectedHotbarSlot;
    public List<ItemStackSaveData> inventory = new ArrayList<>();
}
