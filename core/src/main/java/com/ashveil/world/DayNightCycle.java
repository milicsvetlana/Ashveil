package com.ashveil.world;

import com.ashveil.Config;

import static com.ashveil.world.DayPhase.DAY;

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
        this.dayPhase = DAY;
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
                    dayPhase = DAY;
                    justBecameDay = true;
                    dayCount++;
                }
            }
        }
    }

    public static float getPhaseDuration(DayPhase dayPhase, int dayCount){
        if (dayPhase == null) throw new IllegalArgumentException("Day phase cannot be null");
        if (dayCount < 1) throw new IllegalArgumentException("Day count must be at least 1");

        return switch (dayPhase){
            case DAY ->
                dayCount == 1 ? Config.FIRST_DAY_DURATION : Config.DAY_DURATION;
            case DUSK -> Config.DUSK_DURATION;
            case NIGHT -> Config.NIGHT_DURATION;
        };
    }

    public void applyPersistentState(int dayCount, DayPhase dayPhase, float phaseTimer){
        float phaseDuration = getPhaseDuration(dayPhase, dayCount);

        if (phaseTimer < 0 || phaseTimer >= phaseDuration) throw new IllegalArgumentException("Invalid phase timer.");

        this.dayCount = dayCount;
        this.dayPhase = dayPhase;
        this.phaseTimer = phaseTimer;
        this.currentPhaseDuration = phaseDuration;

        this.justBecameDay = false;
        this.justBecameNight = false;
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
