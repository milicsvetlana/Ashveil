package com.ashveil.world;

import com.ashveil.Config;

public class DayNightCycle {
    private float phaseTimer;
    private float currentPhaseDuration;
    private DayPhase dayPhase;
    private int dayCount;
    private boolean justBecameNight;
    private boolean justBecameDay;

    public DayNightCycle(){
        this.phaseTimer = 0;
        this.currentPhaseDuration = Config.FIRST_DAY_DURATION;
        this.dayCount = 1;
        this.dayPhase = DayPhase.DAY;
        this.justBecameNight = false;
        this.justBecameDay = false;
    }

    public void update(float delta){
        phaseTimer += delta;
        justBecameNight = false;
        justBecameDay = false;
        switch (dayPhase){
            case DAY -> {
                if (phaseTimer >= currentPhaseDuration){
                    phaseTimer -= currentPhaseDuration;
                    currentPhaseDuration = Config.DUSK_DURATION;
                    dayPhase = DayPhase.DUSK;
                }
            }
            case DUSK -> {
                if(phaseTimer >= currentPhaseDuration){
                    phaseTimer -= currentPhaseDuration;
                    currentPhaseDuration = Config.NIGHT_DURATION;
                    dayPhase = DayPhase.NIGHT;
                    justBecameNight = true;
                }
            }
            case NIGHT -> {
                if (phaseTimer >= currentPhaseDuration){
                    phaseTimer -= currentPhaseDuration;
                    currentPhaseDuration = Config.DAY_DURATION;
                    dayPhase = DayPhase.DAY;
                    justBecameDay = true;
                    dayCount++;
                }
            }
        }
    }
    public float getPhaseProgress(){
        return phaseTimer / currentPhaseDuration; //koristi se za sat
    }

    public float getPhaseTimer() {return phaseTimer;}
    public int getDayCount() {return dayCount;}
    public boolean justBecameNight() {return justBecameNight;}
    public boolean isNight(){return dayPhase == DayPhase.NIGHT;}
    public DayPhase getDayPhase() {return dayPhase;}
    public boolean justBecameDay() {return justBecameDay;}
}
