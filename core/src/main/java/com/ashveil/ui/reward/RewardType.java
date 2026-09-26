package com.ashveil.ui.reward;

public enum RewardType {
    SWIFT_STEP("reward.swiftStep.description"),
    BLACKTHORN_CRAFT("reward.blackthornCraft.description"),
    BLOODTHIRST("reward.bloodthirst.description");

    private String descriptionKey;

    RewardType(String descriptionKey){
        this.descriptionKey = descriptionKey;
    }

    public String getDescriptionKey(){
        return descriptionKey;
    }
}
