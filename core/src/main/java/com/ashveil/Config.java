package com.ashveil;

public class Config {

    /** Tile size and rendering scale */
    public static final int TILE_SIZE = 16;
    public static final float SCALE = 2.5f;
    public static final float TILE_DRAW_SIZE = TILE_SIZE * SCALE;

    /** World and screen dimensions */
    public static final int WORLD_WIDTH = 100;
    public static final int WORLD_HEIGHT = 100;
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    /** Inventory */
    public static final int INVENTORY_SIZE = 5;

    /** Player stats and combat */
    public static final float PLAYER_SPEED = 100f;
    public static final float DAMAGE_COOLDOWN_MAX = 1f;
    public static final float PLAYER_ATTACK_HARVEST_RANGE = 60f;
    public static final float PLAYER_ATTACK_MIN_DOT = 0.75f; /*For smaller angle, increase*/
    public static final float PLAYER_HARVEST_MIN_DOT = 0.55f;
    public static final float PLAYER_ATTACK_COOLDOWN = 0.1f;
    public static final float PLAYER_HARVEST_COOLDOWN = 0.5f;
    public static final float PLAYER_PICKUP_RANGE = TILE_SIZE * 2;

    /** Day/night cycle */
    public static final float DAY_DURATION = 100f;
    public static final float NIGHT_DURATION = 5f;

    /** Enemy stats */
    public static final float ZOMBIE_SPEED = 60f;

    /** HP values */
    public static final int PLAYER_HP = 6;
    public static final int ZOMBIE_HP = 15;
    public static final int TREE_HP = 3;
    public static final int ROCK_HP = 3;
}
