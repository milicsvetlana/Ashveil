package com.ashveil.rendering;

import com.ashveil.Config;
import com.ashveil.entities.enemies.Enemy;
import com.ashveil.farming.Crop;
import com.ashveil.farming.GrowablePlant;
import com.ashveil.farming.Sapling;
import com.ashveil.objects.DestructibleObject;
import com.ashveil.world.CameraController;
import com.ashveil.world.World;
import com.ashveil.world.WorldItem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.ashveil.world.TileMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;

public class WorldRenderer {

    private ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;

    private Texture farmTileTexture;
    private Texture wheatTexture;
    private TextureRegion[] wheatStages;
    private Texture saplingTexture;
    private TextureRegion[] saplingStages;

    public WorldRenderer(TileMap tileMap) {
        shapeRenderer = new ShapeRenderer();
        tiledMap = tileMap.getTiledMap();
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, Config.SCALE);

        spriteBatch = new SpriteBatch();
        setTextures();
    }

    public void setTextures(){
        farmTileTexture = new Texture("textures/farming/farm_tile.png");
        wheatTexture = new Texture("textures/farming/wheat_stages.png");
        TextureRegion[][] regions = TextureRegion.split(wheatTexture, 16, 16);
        wheatStages = regions[0];
        saplingTexture = new Texture("textures/farming/tree_stages.png");
        regions = TextureRegion.split(saplingTexture, 16, 32);
        saplingStages = regions[0];
    }

    public void render(World world, CameraController cameraController) {
        tiledMapRenderer.setView(cameraController.camera);
        tiledMapRenderer.render();

        spriteBatch.setProjectionMatrix(cameraController.camera.combined);
        spriteBatch.begin();
        drawFarmingTextures(world);
        spriteBatch.end();

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

        for (DestructibleObject o : world.getDestructibleObjects()) {
            switch(o.getType()){
                case TREE -> {
                    shapeRenderer.setColor(Color.GREEN);
                    break;
                }
                case ROCK -> {
                    shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
                    break;
                }
                case FENCE -> {
                    shapeRenderer.setColor(0.55f, 0.27f, 0.07f, 1f);
                    break;
                }
                case CHEST -> {
                    shapeRenderer.setColor(0.60f, 0.38f, 0.12f, 1f);
                    break;
                }
            }

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

    public void drawFarmingTextures(World world){
        //DRAWING TILLED
        for (int x=0; x < world.getTileMap().getWidth(); x++){
            for (int y=0; y < world.getTileMap().getHeight(); y++){
                if (!world.getFarmingSystem().isTilled(x, y)) continue;
                spriteBatch.draw(farmTileTexture, x * Config.TILE_DRAW_SIZE, y * Config.TILE_DRAW_SIZE, Config.TILE_DRAW_SIZE, Config.TILE_DRAW_SIZE);
            }
        }
        //DRAWING CROPS
        for (int x=0; x < world.getTileMap().getWidth(); x++){
            for (int y=0; y < world.getTileMap().getHeight(); y++){
                GrowablePlant plant = world.getFarmingSystem().getPlant(x, y);
                if (plant == null) continue;

                if (plant instanceof Crop crop){
                    TextureRegion cropTexture = switch (plant.getGrowthStage()) {
                        case EARLY -> wheatStages[0];
                        case MIDDLE -> wheatStages[1];
                        case LATE -> wheatStages[2];
                        case MATURE -> wheatStages[3];
                    };
                    spriteBatch.draw(cropTexture, x * Config.TILE_DRAW_SIZE, y * Config.TILE_DRAW_SIZE, Config.TILE_DRAW_SIZE, Config.TILE_DRAW_SIZE);

                }
                else if (plant instanceof Sapling sapling){
                    TextureRegion saplingTexture = switch (sapling.getGrowthStage()) {
                        case EARLY -> saplingStages[0];
                        case MIDDLE -> saplingStages[1];
                        case LATE -> saplingStages[2];
                        case MATURE -> saplingStages[3];
                    };

                    spriteBatch.draw(
                        saplingTexture,
                        x * Config.TILE_DRAW_SIZE,
                        y * Config.TILE_DRAW_SIZE,
                        Config.TILE_DRAW_SIZE,
                        Config.TILE_DRAW_SIZE * 2
                    );
                }
            }
        }
    }

    public int getMapWidthInTiles() {return tiledMap.getProperties().get("width", Integer.class);}
    public int getMapHeightInTiles() {return tiledMap.getProperties().get("height", Integer.class);}
    public float getMapRenderWidth() {return getMapWidthInTiles() * Config.TILE_DRAW_SIZE;}
    public float getMapRenderHeight() {return getMapHeightInTiles() * Config.TILE_DRAW_SIZE;}

    public void dispose() {
        shapeRenderer.dispose();
        tiledMapRenderer.dispose();
        spriteBatch.dispose();

        farmTileTexture.dispose();
        wheatTexture.dispose();
        saplingTexture.dispose();
    }
}
