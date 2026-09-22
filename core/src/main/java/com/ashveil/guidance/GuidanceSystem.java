package com.ashveil.guidance;

import com.ashveil.progression.ProgressionState;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class GuidanceSystem {
    private final List<GuideStep> mainSequence;
    private int currentStepIndex;
    private boolean triggerSatisfied;
    private boolean messageAcknowledged;
    private final EnumSet<GuideStep> shownContextualSteps;
    private GuideStep activeContextualStep;

    public GuidanceSystem(){
        mainSequence = List.of(
            GuideStep.MOVEMENT,
            GuideStep.STARTER_CHEST,
            GuideStep.PLANTING,
            GuideStep.COLLECT_WOOD,
            GuideStep.OPEN_CRAFTING,
            GuideStep.CRAFT_EQUIPMENT,
            GuideStep.DUSK_WARNING
        );

        currentStepIndex = 0;
        triggerSatisfied = false;
        messageAcknowledged = false;
        shownContextualSteps = EnumSet.noneOf(GuideStep.class);
        activeContextualStep = null;
    }

    public GuideStep getCurrentStep(){
        if (isComplete()) return null;
        return mainSequence.get(currentStepIndex);
    }

    public boolean handleEvent(GameEvent event){
        if (event == null || isComplete()) return false;

        GuideStep currentStep = getCurrentStep();
        GuideTrigger trigger = currentStep.getTrigger();

        if (trigger == null || !trigger.matches(event)) return false;

        triggerSatisfied = true;
        return tryAdvance();
    }

    public boolean handleEvent(GameEvent event, ProgressionState progressionState){
        if (event == null) throw new IllegalArgumentException("Game event cannot be null.");
        if (progressionState == null) throw new IllegalArgumentException("Progression state cannot be null.");

        if (event == GameEvent.OLD_JETTY_DISCOVERED){
            if (!progressionState.isBoatKitCrafted()){
                activateContextualStep(GuideStep.OLD_JETTY_FOUND);
            }
            return false;
        }

        if (event == GameEvent.BOAT_KIT_CRAFTED){
            if (progressionState.isOldJettyFound()){
                activateContextualStep(GuideStep.BOAT_KIT_RETURN_TO_JETTY);
            }
            else{
                activateContextualStep(GuideStep.BOAT_KIT_FIND_JETTY);
            }
            return false;
        }

        if (event == GameEvent.BOAT_BUILT) return false;

        return handleEvent(event);
    }

    private boolean tryAdvance(){
        if (!triggerSatisfied || !messageAcknowledged) return false;
        currentStepIndex++;

        triggerSatisfied = false;
        messageAcknowledged = false;

        return true;
    }

    public boolean acknowledgeCurrentMessage(){
        if (isComplete()) return false;
        messageAcknowledged = true;
        return tryAdvance();
    }

    public boolean isComplete(){
        return currentStepIndex >= mainSequence.size();
    }

    public boolean isCurrentTriggerSatisfied() {return triggerSatisfied;}

    public boolean wasContextualStepShown(GuideStep step){
        if (step == null) throw new IllegalArgumentException("Guide Step cannot be null.");
        return shownContextualSteps.contains(step);
    }

    public boolean activateContextualStep(GuideStep step){
        if (step == null) throw new IllegalArgumentException("Guide step cannot be null.");

        if (shownContextualSteps.contains(step)) return false;
        if (activeContextualStep != null) return false;

        activeContextualStep = step;
        return true;
    }

    public boolean acknowledgeActiveContextualStep(){
        if (activeContextualStep == null) return false;

        shownContextualSteps.add(activeContextualStep);
        activeContextualStep = null;
        return true;
    }

    public Set<GuideStep> getShownContextualSteps(){
        return EnumSet.copyOf(shownContextualSteps);
    }

    public boolean isMessageAcknowledged(){
        return messageAcknowledged;
    }

    public boolean shouldShowCurrentMessage(){
        GuideStep currentStep = getCurrentStep();

        if (currentStep == null) return false;
        if (messageAcknowledged) return false;

        if (currentStep == GuideStep.DUSK_WARNING && !triggerSatisfied) return false;
        //ako smo sacuvali dok je bio dan, da nam se ne pojavi opet to za Dusk
        return true;
    }

    public void applyPersistentState(GuideStep currentStep, boolean triggerSatisfied, boolean messageAcknowledged,
                                     Set<GuideStep> shownContextualSteps, GuideStep activeContextualStep){
        if (currentStep == null){
            currentStepIndex = mainSequence.size();
            this.triggerSatisfied = false;
            this.messageAcknowledged = false;
        }
        else{
            int restoredIndex = mainSequence.indexOf(currentStep);
            if (restoredIndex < 0) throw new IllegalArgumentException("Guide step is not part of the main sequence.");

            currentStepIndex = restoredIndex;
            this.triggerSatisfied = triggerSatisfied;
            this.messageAcknowledged = messageAcknowledged;
        }

        this.shownContextualSteps.clear();

        if (shownContextualSteps != null) {
            this.shownContextualSteps.addAll(shownContextualSteps);
        }

        this.activeContextualStep = activeContextualStep;
    }

    public GuideStep getActiveContextualStep(){return activeContextualStep;}

}
