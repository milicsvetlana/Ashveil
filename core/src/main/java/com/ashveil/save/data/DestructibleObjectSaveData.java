package com.ashveil.save.data;

import java.util.ArrayList;
import java.util.List;

public class DestructibleObjectSaveData {
    public String objectType;
    public float x;
    public float y;
    public int currentHp;
    public List<ItemStackSaveData> chestInventory = new ArrayList<>();
}
