package com.ashveil.guidance;

public enum GuideStep {
    MOVEMENT("guidance.movement", new GuideTrigger(GameEvent.PLAYER_MOVED)),
    STARTER_CHEST("guidance.starterChest", new GuideTrigger(GameEvent.CHEST_CLOSED)),
    PLANTING("guidance.planting", new GuideTrigger(GameEvent.SEED_PLANTED)),
    COLLECT_WOOD("guidance.collectWood", new GuideTrigger(GameEvent.WOOD_COLLECTED)),
    OPEN_CRAFTING("guidance.openCrafting", new GuideTrigger(GameEvent.CRAFTING_OPENED)),
    CRAFT_EQUIPMENT("guidance.craftEquipment", new GuideTrigger(GameEvent.ITEM_CRAFTED)),
    HEALING("guidance.healing", null),
    DUSK_WARNING("guidance.dusk", new GuideTrigger(GameEvent.DUSK_STARTED)),

    BOAT_KIT_FIND_JETTY("guidance.boatKitFindJetty", null),
    OLD_JETTY_FOUND("guidance.oldJettyFound", null),
    BOAT_KIT_RETURN_TO_JETTY("guidance.boatKitReturnToJetty", null),
    BOAT_BUILT("guidance.boatBuilt", null),

    WINDY_ARRIVAL("guidance.windyArrival", null),
    WINDY_GUARDIAN_STARTED("guidance.windyGuardianStarted", null),
    WINDY_SCROLL_I_READ("guidance.windyScrollIRead", null),
    WISP_GLOBAL_UNLOCKED("guidance.wispGlobalUnlocked", null),

    DARKROOT_ARRIVAL("guidance.darkrootArrival", null),
    DARKROOT_SCROLL_II_READ("guidance.darkrootScrollIIRead", null);

    private final String messageKey;
    private final GuideTrigger trigger;

    GuideStep (String messageKey, GuideTrigger trigger){
        this.messageKey = messageKey;
        this.trigger = trigger;
    }

    public String getMessageKey(){return messageKey;}
    public GuideTrigger getTrigger(){return trigger;}
}
