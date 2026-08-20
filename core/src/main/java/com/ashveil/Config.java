package com.ashveil;

public class Config {

    /** World scale and rendering */
    public static final int TILE_SIZE = 16;
    public static final float SCALE = 2.5f;
    public static final float TILE_DRAW_SIZE = TILE_SIZE * SCALE;

    /** Screen and camera */
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final float CAMERA_SMOOTHING = 12f;

    /** Player movement and interaction */
    public static final float PLAYER_SPEED = 100f;
    public static final float PLAYER_PICKUP_RANGE = TILE_SIZE * 2;
    public static final float PLAYER_TARGET_RANGE = 5;

    /** Player combat */
    public static final int PLAYER_BASE_DAMAGE = 1;
    public static final float DAMAGE_COOLDOWN_MAX = 1f;
    public static final float PLAYER_PRIMARY_ACTION_RANGE = 60f;
    public static final float PLAYER_PRIMARY_ACTION_MIN_DOT = 0.75f;
    public static final float PLAYER_PRIMARY_ACTION_COOLDOWN = 0.75f;

    /** Player health and death */
    public static final int PLAYER_HP = 10;
    public static final int PLAYER_HEART_SLOTS = 5;
    public static final int HP_PER_HEART = 2;
    public static final int MAX_BROKEN_HEARTS = 3;
    public static final int BREAD_HEALING = 1;
    public static final float DEATH_FADE_DURATION = 2f;

    /** Inventory */
    public static final int INVENTORY_SIZE = 20;
    public static final int CHEST_INVENTORY_SIZE = 15;
    public static final int HOTBAR_SIZE = 5;
    public static final int SLOT_NOT_SELECTED = -1;
    public static final int SLOT_MOVE = 1;

    /** Day and night cycle */
    public static final float DAY_DURATION = 5f;
    public static final float NIGHT_DURATION = 5f;

    /** Ground items */
    public static final float WORLD_ITEM_MERGE_RANGE = TILE_SIZE * 2;
    public static final float WORLD_ITEM_DESPAWN_TIME = 300f;
    public static final int WORLD_MAX_NUMBER_OF_ITEMS = 250;

    /** Resource generation */
    public static final int MAX_EXTRA_INITIAL_RESOURCES = 49;
    public static final int MIN_INITIAL_RESOURCES = 10;
    public static final int INITIAL_SPAWN_CLEAR_RADIUS = 2;

    /** Resource stats and drops */
    public static final int TREE_HP = 3;
    public static final int ROCK_HP = 3;
    public static final int FENCE_HP = 4;
    public static final int CHEST_HP = 4;
    public static final int FIRST_TREE_DROP_AMOUNT = 2;

    /** Enemy lifecycle */
    public static final float ENEMY_DYING_DURATION = 0.5f;
    public static final float ENEMY_HP_BAR_DURATION = 1.5f;
    public static final float ENEMY_HIT_FLASH_DURATION = 0.15f;

    /** Gold drops */
    public static final int DOUBLE_GOLD_DROP_CHANCE = 5;
    public static final int SINGLE_GOLD_DROP_CHANCE = 30;

    public static final int SAPLING_DROP_CHANCE = 40; // npr 40% šanse

    /** Farming */
    public static final float WHEAT_EARLY_DURATION = 3f;
    public static final float WHEAT_MIDDLE_DURATION = 3f;
    public static final float WHEAT_LATE_DURATION = 3f;
    public static final float SAPLING_EARLY_DURATION = 5f;
    public static final float SAPLING_MIDDLE_DURATION = 5f;
    public static final float SAPLING_LATE_DURATION = 5f;
}
