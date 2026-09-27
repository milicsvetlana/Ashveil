package com.ashveil.encounter;

import com.ashveil.Config;
import com.badlogic.gdx.scenes.scene2d.actions.IntAction;

import java.util.Random;

public class CrimsonVeilSystem {
    private final Random random;

    private CrimsonVeilState state;
    private int nextVeilDay;
    private int completedVeils;

    private int currentWave;
    private int totalThreatBudget;

    private float waveTimer;
    private float clearGraceTimer;
    private float recoveryTimer;

    public CrimsonVeilSystem(){
        random = new Random();

        state = CrimsonVeilState.INACTIVE;
        completedVeils = 0;
        nextVeilDay = randomBetween(Config.CRIMSON_VEIL_FIRST_DAY_MIN, Config.CRIMSON_VEIL_FIRST_DAY_MAX);

        currentWave = 0;
        totalThreatBudget = 0;

        waveTimer = 0f;
        clearGraceTimer = 0f;
        recoveryTimer = 0f;
    }

    public boolean handleDuskStarted(int dayCount){
        if (state != CrimsonVeilState.INACTIVE) return false;
        if (dayCount != nextVeilDay) return false;

        state = CrimsonVeilState.WARNING;
        return true;
    }

    public boolean handleNightStarted(int dayCount){
        if (state != CrimsonVeilState.WARNING) return false;

        state = CrimsonVeilState.ACTIVE;
        currentWave = 1;
        waveTimer = 0f;
        clearGraceTimer = 0f;

        int ordinaryNightBudget = Config.INITIAL_NIGHT_THREAT_BUDGET + (dayCount - 1) * Config.NIGHT_THREAT_BUDGET_INCREASE;

        totalThreatBudget = ordinaryNightBudget + Config.CRIMSON_VEIL_BASE_BONUS_BUDGET
                            + completedVeils * Config.CRIMSON_VEIL_COMPLETED_BONUS_BUDGET;

        return true;
    }

    public int getCurrentWaveBudget(){
        if (currentWave < 1 || currentWave > 3) return 0;

        int waveOneBudget = Math.round(totalThreatBudget * Config.CRIMSON_VEIL_WAVE_1_SHARE);
        int waveTwoBudget = Math.round(totalThreatBudget * Config.CRIMSON_VEIL_WAVE_2_SHARE);

        return switch (currentWave){
            case 1 -> waveOneBudget;
            case 2 -> waveTwoBudget;
            case 3 -> totalThreatBudget - waveOneBudget - waveTwoBudget;
            default -> 0;
        };
    }

    public boolean shouldAdvanceWave(float delta, boolean hasLivingEnemies, boolean hasPendingSpawns){
        if (state != CrimsonVeilState.ACTIVE) return false;

        waveTimer += delta;
        boolean waveCleared = !hasLivingEnemies && !hasPendingSpawns;

        if (waveCleared) clearGraceTimer += delta;
        else clearGraceTimer = 0f;

        if (waveCleared && clearGraceTimer >= Config.CRIMSON_VEIL_CLEAR_GRACE_DURATION) return true;

        return currentWave < Config.CRIMSON_VEIL_WAVE_COUNT && waveTimer >= Config.CRIMSON_VEIL_WAVE_MAX_DURATION;
    }

    public boolean hasNextWave(){
        return currentWave < Config.CRIMSON_VEIL_WAVE_COUNT;
    }

    public void advanceWave(){
        if (state != CrimsonVeilState.ACTIVE) return;
        if (!hasNextWave()) return;

        currentWave++;
        waveTimer = 0f;
        clearGraceTimer = 0f;
    }

    public void restartActiveVeil(){
        if (state != CrimsonVeilState.ACTIVE) return;

        currentWave = 1;
        waveTimer = 0f;
        clearGraceTimer = 0f;
        recoveryTimer = 0f;
    }

    public void completeVeil(int dayCount){
        if (state != CrimsonVeilState.ACTIVE) return;

        state = CrimsonVeilState.RECOVERY;
        completedVeils++;

        waveTimer = 0f;
        clearGraceTimer = 0f;

        nextVeilDay = dayCount + randomBetween(Config.CRIMSON_VEIL_INTERVAL_MIN, Config.CRIMSON_VEIL_INTERVAL_MAX);
    }

    public boolean updateRecovery(float delta){
        if (state != CrimsonVeilState.RECOVERY) return false;
        recoveryTimer += delta;
        return recoveryTimer >= Config.CRIMSON_VEIL_RECOVERY_DURATION;
    }

    public void finishRecovery(){
        if (state != CrimsonVeilState.RECOVERY) return;

        state = CrimsonVeilState.INACTIVE;

        currentWave = 0;
        waveTimer = 0f;
        clearGraceTimer = 0f;
        recoveryTimer = 0f;
    }

    private int randomBetween(int min, int max){
        return min + random.nextInt(max - min + 1);
    }

    public void applyPersistentState(CrimsonVeilState state, int nextVeilDay, int completedVeils, int currentWave,
                                     int totalThreatBudget, float waveTimer, float clearGraceTimer, float recoveryTimer){
        if (state == null) throw new IllegalArgumentException("Crimson Veil state cannot be null");
        if (nextVeilDay < 1) throw new IllegalArgumentException("Next Veil day must be positive");
        if (completedVeils < 0) throw new IllegalArgumentException("Completed Veils cannot be negative");
        if (currentWave < 0 || currentWave > Config.CRIMSON_VEIL_WAVE_COUNT) throw new IllegalArgumentException("Invalid Crimson Veil wave");
        if (totalThreatBudget < 0) throw new IllegalArgumentException("Threat budget cannot be negative");
        if (waveTimer < 0f || clearGraceTimer < 0f || recoveryTimer < 0f)
            throw new IllegalArgumentException("Crimson Veil timers cannot be negative");

        this.state = state;
        this.nextVeilDay = nextVeilDay;
        this.completedVeils = completedVeils;
        this.currentWave = currentWave;
        this.totalThreatBudget = totalThreatBudget;
        this.waveTimer = waveTimer;
        this.clearGraceTimer = clearGraceTimer;
        this.recoveryTimer = recoveryTimer;
    }

    public CrimsonVeilState getState() {return state;}
    public int getNextVeilDay() {return nextVeilDay;}
    public int getCompletedVeils() {return completedVeils;}
    public boolean isWarning() {return state == CrimsonVeilState.WARNING;}
    public boolean isActive() {return state == CrimsonVeilState.ACTIVE;}
    public boolean isRecovery() {return state == CrimsonVeilState.RECOVERY;}
    public int getCurrentWave() {return currentWave;}
    public int getTotalThreatBudget(){return totalThreatBudget;}
    public float getWaveTimer(){return waveTimer;}
    public float getClearGraceTimer(){return clearGraceTimer;}
    public float getRecoveryTimer(){return recoveryTimer;}
}
