package com.ashveil.rendering;

import com.ashveil.Config;
import com.ashveil.combat.Projectile;
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
    private Texture projectileTexture;
    private TextureRegion[] projectileRegions;

    public EnemyRenderer(){
        textures = new EnumMap<>(EnemyType.class);
        regions = new EnumMap<>(EnemyType.class);

        loadEnemyTexture(EnemyType.SHADE, "textures/entities/enemies/shade_idle.png");
        loadEnemyTexture(EnemyType.WISP, "textures/entities/enemies/wisp_idle.png");
        loadEnemyTexture(EnemyType.WRAITH, "textures/entities/enemies/wraith_idle.png");
    }

    private void loadEnemyTexture(EnemyType enemyType, String path){
        Texture texture = new Texture(path);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[][] split = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT);
        textures.put(enemyType, texture);
        regions.put(enemyType, split[0]);

        wispChargeTexture = new Texture("textures/entities/enemies/wisp_charge.png");
        wispChargeTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        wispChargeRegions = TextureRegion.split(wispChargeTexture, FRAME_WIDTH, FRAME_HEIGHT)[0];

        projectileTexture = new Texture("textures/entities/enemies/projectile.png");
        projectileTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        projectileRegions = TextureRegion.split(projectileTexture, FRAME_WIDTH, FRAME_HEIGHT)[0];
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
        else batch.setColor(1f, 1f, 1f, enemy.getRenderAlpha());
        batch.draw(region, drawX, drawY, drawWidth, drawHeight);
        batch.setColor(1f, 1f, 1f, enemy.getRenderAlpha());
    }

    public void renderProjectile(Projectile projectile, SpriteBatch batch){
        int frameIndex = getProjectileFrameIndex(projectile);
        TextureRegion region = projectileRegions[frameIndex];
        float drawWidth = Config.TILE_DRAW_SIZE;
        float drawHeight = Config.TILE_DRAW_SIZE;

        float centerX = (projectile.getCollisionBounds().x + projectile.getCollisionBounds().width / 2f) * Config.SCALE;
        float centerY = (projectile.getCollisionBounds().y + projectile.getCollisionBounds().height / 2f) * Config.SCALE;

        float drawX = centerX - drawWidth / 2f;
        float drawY = centerY - drawHeight / 2f;

        batch.draw(region, drawX, drawY, drawWidth, drawHeight);
    }

    private int getFrameIndex(Facing facing){
        return switch (facing){
            case DOWN -> 0;
            case UP -> 1;
            case LEFT -> 2;
            case RIGHT -> 3;
        };
    }

    private int getProjectileFrameIndex(Projectile projectile){
        float velocityX = projectile.getVelocityX();
        float velocityY = projectile.getVelocityY();

        if (Math.abs(velocityX) > Math.abs(velocityY)) return velocityX > 0 ? 3 : 2;
        return velocityY > 0 ? 1 : 0;
    }

    public void dispose(){
        for (Texture texture : textures.values()) texture.dispose();
        wispChargeTexture.dispose();
        projectileTexture.dispose();
    }

}
