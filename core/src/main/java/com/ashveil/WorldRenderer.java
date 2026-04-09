package com.ashveil;

import com.ashveil.entities.ZombieEnemy;
import com.ashveil.objects.ResourceObject;
import com.ashveil.objects.ResourceType;
import com.ashveil.world.CameraController;
import com.ashveil.world.TileType;
import com.ashveil.world.World;
import com.ashveil.world.WorldItem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

public class WorldRenderer {

    private ShapeRenderer shapeRenderer;

    public WorldRenderer() {
        shapeRenderer = new ShapeRenderer();
    }

    public void render(World world, CameraController cameraController) {
        shapeRenderer.setProjectionMatrix(cameraController.camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < Config.WORLD_HEIGHT; y++) {
            for (int x = 0; x < Config.WORLD_WIDTH; x++) {
                TileType tile = world.getTileMap().getTile(x, y);

                if (tile == TileType.GRASS) {
                    shapeRenderer.setColor(0.2f, 0.6f, 0.2f, 1f);
                } else if (tile == TileType.WATER) {
                    shapeRenderer.setColor(0.1f, 0.3f, 0.8f, 1f);
                } else if (tile == TileType.SAND) {
                    shapeRenderer.setColor(0.9f, 0.8f, 0.5f, 1f);
                }

                shapeRenderer.rect(
                    x * Config.TILE_DRAW_SIZE,
                    y * Config.TILE_DRAW_SIZE,
                    Config.TILE_DRAW_SIZE,
                    Config.TILE_DRAW_SIZE
                );
            }
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(1f, 1f, 0f, 1f);
        shapeRenderer.rect(
            world.getPlayer().getX() * Config.SCALE,
            world.getPlayer().getY() * Config.SCALE,
            Config.TILE_DRAW_SIZE,
            Config.TILE_DRAW_SIZE
        );

        shapeRenderer.setColor(1f, 0f, 0f, 1f);
        for (ZombieEnemy z : world.getZombies()) {
            shapeRenderer.rect(
                z.getX() * Config.SCALE,
                z.getY() * Config.SCALE,
                Config.TILE_DRAW_SIZE,
                Config.TILE_DRAW_SIZE
            );
        }

        for (ResourceObject o : world.getResourceObjects()) {
            if (o.getType() == ResourceType.TREE) shapeRenderer.setColor(0.1f, 0.4f, 0.1f, 1f);
            else shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
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

    public void dispose() {
        shapeRenderer.dispose();
    }
}
