package com.ashveil.rendering;

import com.ashveil.Config;
import com.ashveil.entities.enemies.Enemy;
import com.ashveil.objects.DestructibleObject;
import com.ashveil.objects.DestructibleObjectType;
import com.ashveil.world.CameraController;
import com.ashveil.world.World;
import com.ashveil.world.WorldItem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.ashveil.world.TileMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;

public class WorldRenderer {

    private ShapeRenderer shapeRenderer;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;

    public WorldRenderer(TileMap tileMap) {
        shapeRenderer = new ShapeRenderer();
        tiledMap = tileMap.getTiledMap();
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, Config.SCALE);
    }

    public void render(World world, CameraController cameraController) {
        tiledMapRenderer.setView(cameraController.camera);
        tiledMapRenderer.render();

        shapeRenderer.setProjectionMatrix(cameraController.camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(1f, 1f, 0f, 1f);
        shapeRenderer.rect(
            world.getPlayer().getX() * Config.SCALE,
            world.getPlayer().getY() * Config.SCALE,
            Config.TILE_DRAW_SIZE,
            Config.TILE_DRAW_SIZE
        );

        shapeRenderer.setColor(1f, 0f, 0f, 1f);
        for (Enemy e : world.getEnemies()) {
            if (e.getHitFlashTimer() > 0) {
                shapeRenderer.setColor(1f, 1f, 1f, 1f);
            } else {
                shapeRenderer.setColor(1f, 0f, 0f, 1f);
            }
            shapeRenderer.rect(
                e.getX() * Config.SCALE,
                e.getY() * Config.SCALE,
                Config.TILE_DRAW_SIZE,
                Config.TILE_DRAW_SIZE
            );
        }

        for (DestructibleObject o : world.getDestructibleObject()) {
            if (o.getType() == DestructibleObjectType.TREE) shapeRenderer.setColor(0.1f, 0.4f, 0.1f, 1f);
            else if (o.getType() == DestructibleObjectType.ROCK) shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
            else shapeRenderer.setColor(0.55f, 0.27f, 0.07f, 1f);
            shapeRenderer.rect(
                o.getX() * Config.SCALE,
                o.getY() * Config.SCALE,
                Config.TILE_DRAW_SIZE,
                Config.TILE_DRAW_SIZE
            );
        }

        shapeRenderer.setColor(0.5f, 0.5f, 0f, 1f);
        for (WorldItem i : world.getGroundItems()) {
            shapeRenderer.rect(
                i.getX() * Config.SCALE,
                i.getY() * Config.SCALE,
                Config.TILE_DRAW_SIZE,
                Config.TILE_DRAW_SIZE
            );
        }

        shapeRenderer.end();

        if (world.getDayNightCycle().isNight()) {
            shapeRenderer.setProjectionMatrix(
                new Matrix4().setToOrtho2D(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT)
            );

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0.3f, 0.5f);
            shapeRenderer.rect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
            shapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    public void renderTargetPreview(CameraController cameraController, float worldX, float worldY, boolean valid){
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        if (valid) shapeRenderer.setColor(0f, 1f, 0f, 1f);
        else shapeRenderer.setColor(1f, 0f, 0f, 1f);

        shapeRenderer.rect(worldX * Config.SCALE, worldY * Config.SCALE, Config.TILE_DRAW_SIZE, Config.TILE_DRAW_SIZE);
        shapeRenderer.end();
    }

    public int getMapWidthInTiles() {
        return tiledMap.getProperties().get("width", Integer.class);
    }

    public int getMapHeightInTiles() {
        return tiledMap.getProperties().get("height", Integer.class);
    }

    public float getMapRenderWidth() {
        return getMapWidthInTiles() * Config.TILE_DRAW_SIZE;
    }

    public float getMapRenderHeight() {
        return getMapHeightInTiles() * Config.TILE_DRAW_SIZE;
    }

    public void dispose() {
        shapeRenderer.dispose();
        tiledMapRenderer.dispose();
    }
}
