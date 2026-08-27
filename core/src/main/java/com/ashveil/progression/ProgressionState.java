package com.ashveil.progression;

public final class ProgressionState {
    private boolean firstTreeDropClaimed;
    private boolean wispNightUnlocked;
    private boolean wraithNightUnlocked;

    public ProgressionState() {
        this.firstTreeDropClaimed = false;
    }

    public boolean isFirstTreeDropClaimed() {return firstTreeDropClaimed;}
    public void claimFirstTreeDrop() {firstTreeDropClaimed = true;}
    public boolean isWispNightUnlocked() {return wispNightUnlocked;}
    public boolean isWraithNightUnlocked() {return wraithNightUnlocked;}
}
