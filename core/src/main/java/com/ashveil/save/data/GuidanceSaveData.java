package com.ashveil.save.data;

import java.util.ArrayList;
import java.util.List;

public class GuidanceSaveData {
    public String currentStep;
    public boolean triggerSatisfied;
    public boolean messageAcknowledged;
    public List<String> shownContextualSteps = new ArrayList<>();
    public String activeContextualStep;
}
