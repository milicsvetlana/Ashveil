package com.ashveil.guidance;

public enum GuideStep {
    MOVEMENT("guidance.movement", new GuideTrigger(GameEvent.PLAYER_MOVED)),
    STARTER_CHEST("guidance.starterChest", new GuideTrigger(GameEvent.CHEST_CLOSED)),
    PLANTING("guidance.planting", new GuideTrigger(GameEvent.SEED_PLANTED)),
    COLLECT_WOOD("guidance.collectWood", new GuideTrigger(GameEvent.WOOD_COLLECTED)),
    OPEN_CRAFTING("guidance.openCrafting", new GuideTrigger(GameEvent.CRAFTING_OPENED)),
    CRAFT_EQUIPMENT("guidance.craftEquipment", new GuideTrigger(GameEvent.ITEM_CRAFTED)),
    HEALING("guidance.healing", null),
    DUSK_WARNING("guidance.dusk", new GuideTrigger(GameEvent.DUSK_STARTED));

    private final String messageKey;
    private final GuideTrigger trigger;

    GuideStep (String messageKey, GuideTrigger trigger){
        this.messageKey = messageKey;
        this.trigger = trigger;
    }

    public String getMessageKey(){return messageKey;}
    public GuideTrigger getTrigger(){return trigger;}
}
