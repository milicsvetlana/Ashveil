package com.ashveil.encounter;

import com.ashveil.Config;

public class AshenRiteSystem {
    private AshenRiteState state;

    private boolean scrollIPlaced;
    private boolean scrollIIPlaced;
    private boolean scrollIIIPlaced;

    private int currentWave;
    private float waveBreakTimer;
    private boolean waitingForNextWave;

    public AshenRiteSystem(){
        state = AshenRiteState.ASSEMBLING;
        scrollIPlaced = false;
        scrollIIPlaced = false;
        scrollIIIPlaced = false;

        currentWave = 0;
        waveBreakTimer = 0f;
        waitingForNextWave = false;
    }

    public void placeScrollI(){
        if (scrollIPlaced) return;
        scrollIPlaced = true;
        updateReadyState();
    }

    public void placeScrollII(){
        if (scrollIIPlaced) return;
        scrollIIPlaced = true;
        updateReadyState();
    }

    public void placeScrollIII(){
        if (scrollIIIPlaced) return;
        scrollIIIPlaced = true;
        updateReadyState();
    }

    private void updateReadyState(){
        if (!areAllScrollsPlaced()) return;
        if (state != AshenRiteState.ASSEMBLING) return;

        state = AshenRiteState.RITE_READY;
    }

    public boolean areAllScrollsPlaced(){
        return scrollIPlaced && scrollIIPlaced && scrollIIIPlaced;
    }

    public boolean pullLever(){
        if (state == AshenRiteState.RITE_READY){
            state = AshenRiteState.REVEAL;
            return true;
        }

        if (state == AshenRiteState.LAST_VEIL_READY){
            beginLastVeilAttempt();
            return true;
        }

        return false;
    }

    public void startLastVeil(){
        if (state != AshenRiteState.REVEAL) return;
        beginLastVeilAttempt();
    }

    public boolean isLeverPulled(){
        return state == AshenRiteState.REVEAL || state == AshenRiteState.LAST_VEIL
                 || state == AshenRiteState.AFTERMATH || state == AshenRiteState.COMPLETED;
    }

    public boolean blocksDayNightCycle(){
        return state == AshenRiteState.REVEAL
            || state == AshenRiteState.LAST_VEIL_READY
            || state == AshenRiteState.LAST_VEIL
            || state == AshenRiteState.AFTERMATH
            || state == AshenRiteState.COMPLETED;
    }

    private void beginLastVeilAttempt(){
        state = AshenRiteState.LAST_VEIL;
        currentWave = 1;
        waveBreakTimer = 0f;
        waitingForNextWave = false;
    }

    public void beginWaveBreak(){
        if (state != AshenRiteState.LAST_VEIL) return;
        if (currentWave >= Config.LAST_VEIL_WAVE_COUNT) return;

        waitingForNextWave = true;
        waveBreakTimer = 0f;
    }

    public boolean updateWaveBreak(float delta){
        if (state != AshenRiteState.LAST_VEIL) return false;
        if (!waitingForNextWave) return false;

        waveBreakTimer += delta;

        if (waveBreakTimer < Config.LAST_VEIL_WAVE_BREAK_DURATION) return false;

        currentWave++;
        waveBreakTimer = 0f;
        waitingForNextWave = false;

        return true;
    }


    public void resetLastVeilAttempt() {
        if (state != AshenRiteState.LAST_VEIL) return;

        state = AshenRiteState.LAST_VEIL_READY;
        currentWave = 0;
        waveBreakTimer = 0f;
        waitingForNextWave = false;
    }

    public void completeLastVeil(){
        if (state != AshenRiteState.LAST_VEIL) return;

        state = AshenRiteState.AFTERMATH;
        currentWave = 0;
        waveBreakTimer = 0f;
        waitingForNextWave = false;
    }

    public void completeGame(){
        if (state != AshenRiteState.AFTERMATH) return;
        state = AshenRiteState.COMPLETED;
    }

    public void applyPersistentState(AshenRiteState state, boolean scrollIPlaced, boolean scrollIIPlaced, boolean scrollIIIPlaced){
        if (state == null) throw new IllegalArgumentException("Ashen Rite state cannot be null.");

        this.scrollIPlaced = scrollIPlaced;
        this.scrollIIPlaced = scrollIIPlaced;
        this.scrollIIIPlaced = scrollIIIPlaced;

        if (state == AshenRiteState.LAST_VEIL) this.state = AshenRiteState.LAST_VEIL_READY;
        else this.state = state;

        currentWave = 0;
        waveBreakTimer = 0f;
        waitingForNextWave = false;
    }

    public AshenRiteState getPersistentState(){
        if (state == AshenRiteState.LAST_VEIL) return AshenRiteState.LAST_VEIL_READY;
        return state;
    }

    public AshenRiteState getState(){return state;}
    public boolean isScrollIPlaced(){return scrollIPlaced;}
    public boolean isScrollIIPlaced(){return scrollIIPlaced;}
    public boolean isScrollIIIPlaced(){return scrollIIIPlaced;}
    public boolean isLastVeil(){return state == AshenRiteState.LAST_VEIL;}
    public boolean isWaitingForNextWave(){return waitingForNextWave;}
    public int getCurrentWave(){return currentWave;}
    public boolean isAftermath(){return state == AshenRiteState.AFTERMATH;}
}
