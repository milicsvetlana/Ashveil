package com.ashveil;

public class Config {

    /** Tile size and rendering scale */
    public static final int TILE_SIZE = 16;
    public static final float SCALE = 2.5f;
    public static final float TILE_DRAW_SIZE = TILE_SIZE * SCALE;

    /** World and screen dimensions */
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final float CAMERA_SMOOTHING = 12f;

    /** Inventory */
    public static final int INVENTORY_SIZE = 20;
    public static final int HOTBAR_SIZE = 5;
    public static final int SLOT_NOT_SELECTED = -1;
    public static final int SLOT_MOVE = 1;

    /** Player stats and combat */
    public static final float PLAYER_SPEED = 100f;
    public static final float DAMAGE_COOLDOWN_MAX = 1f;
    public static final float PLAYER_PRIMARY_ACTION_RANGE = 60f;
    public static final float PLAYER_PRIMARY_ACTION_MIN_DOT = 0.75f; /*For smaller angle, increase*/
    public static final float PLAYER_PRIMARY_ACTION_COOLDOWN = 0.75f;
    public static final float PLAYER_PICKUP_RANGE = TILE_SIZE * 2;

    /** Day/night cycle */
    public static final float DAY_DURATION = 100f;
    public static final float NIGHT_DURATION = 5f;

    /** World stats */
    public static final float WORLD_ITEM_MERGE_RANGE = TILE_SIZE * 2;
    public static final float WORLD_ITEM_DESPAWN_TIME = 300f;
    public static final int WORLD_MAX_NUMBER_OF_ITEMS = 250;

    /** World resources */
    public static final int MAX_EXTRA_INITIAL_RESOURCES = 49;
    public static final int MIN_INITIAL_RESOURCES = 10;
    public static final int INITIAL_SPAWN_CLEAR_RADIUS = 2;

    /** Enemy stats */
    public static final float SHADE_SPEED = 30f;

    /** Damage values */
    public static final int PLAYER_BASE_DAMAGE = 1;

    /** HP values */
    public static final int PLAYER_HP = 6;
    public static final int SHADE_HP = 6;
    public static final int TREE_HP = 1;
    public static final int ROCK_HP = 3;

    public static final int FIRST_TREE_DROP_AMOUNT = 2;
}
