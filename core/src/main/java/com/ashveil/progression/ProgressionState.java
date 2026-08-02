package com.ashveil.progression;

public final class ProgressionState {
    private boolean firstTreeDropClaimed;

    public ProgressionState() {
        this.firstTreeDropClaimed = false;
    }

    public boolean isFirstTreeDropClaimed() {return firstTreeDropClaimed;}
    public void claimFirstTreeDrop() {firstTreeDropClaimed = true;}
}
