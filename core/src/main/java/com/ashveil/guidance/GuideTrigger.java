package com.ashveil.guidance;

public class GuideTrigger {
    private final GameEvent expectedEvent;

    public GuideTrigger(GameEvent expectedEvent){
        if (expectedEvent == null) throw new IllegalArgumentException("Expected event cannot be null.");
        this.expectedEvent = expectedEvent;
    }

    public boolean matches(GameEvent event){
        return expectedEvent == event;
    }



}
