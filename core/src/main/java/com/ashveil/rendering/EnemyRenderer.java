package com.ashveil.rendering;

import com.ashveil.Config;
import com.ashveil.entities.Facing;
import com.ashveil.entities.enemies.Enemy;
import com.ashveil.entities.enemies.EnemyType;
import com.ashveil.entities.enemies.Wisp;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

public class EnemyRenderer {
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 64;
    private static final float ENEMY_RENDER_SCALE = 3f;

    private final Map<EnemyType, Texture> textures;
    private final Map<EnemyType, TextureRegion[]> regions;
    private Texture wispChargeTexture;
    private TextureRegion[] wispChargeRegions;

    public EnemyRenderer(){
        textures = new EnumMap<>(EnemyType.class);
        regions = new EnumMap<>(EnemyType.class);

        loadEnemyTexture(EnemyType.SHADE, "textures/entities/enemies/shade_idle.png");
        loadEnemyTexture(EnemyType.WISP, "textures/entities/enemies/wisp_idle.png");
        loadEnemyTexture(EnemyType.WRAITH, "textures/entities/enemies/wraith_idle.png");
    }

    private void loadEnemyTexture(EnemyType enemyType, String path){
        Texture texture = new Texture(path);
        TextureRegion[][] split = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT);
        textures.put(enemyType, texture);
        regions.put(enemyType, split[0]);

        wispChargeTexture = new Texture("textures/entities/enemies/wisp_charge.png");
        wispChargeTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        wispChargeRegions = TextureRegion.split(wispChargeTexture, FRAME_WIDTH, FRAME_HEIGHT)[0];
    }

    public void render(Enemy enemy, SpriteBatch batch){
        TextureRegion[] enemyRegions = regions.get(enemy.getEnemyType());
        if (enemy instanceof Wisp wisp && wisp.isCharging()) enemyRegions = wispChargeRegions;
        if (enemyRegions == null) return;

        int frameIndex = getFrameIndex(enemy.getFacing());
        TextureRegion region = enemyRegions[frameIndex];

        float drawWidth = Config.TILE_DRAW_SIZE * ENEMY_RENDER_SCALE;
        float drawHeight = Config.TILE_DRAW_SIZE * ENEMY_RENDER_SCALE;
        float drawX = enemy.getX() * Config.SCALE - (drawWidth - Config.TILE_DRAW_SIZE) / 2f;
        float drawY = enemy.getY() * Config.SCALE;

        if (enemy.getHitFlashTimer() > 0) batch.setColor(1f, 0.4f, 0.4f, 1f);
        else batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(region, drawX, drawY, drawWidth, drawHeight);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private int getFrameIndex(Facing facing){
        return switch (facing){
            case DOWN -> 0;
            case UP -> 1;
            case LEFT -> 2;
            case RIGHT -> 3;
        };
    }

    public void dispose(){
        for (Texture texture : textures.values()) texture.dispose();
        wispChargeTexture.dispose();
    }

}
