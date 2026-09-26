package com.ashveil.rendering;

import com.ashveil.Config;
import com.ashveil.combat.Projectile;
import com.ashveil.entities.enemies.Enemy;
import com.ashveil.farming.Crop;
import com.ashveil.farming.GrowablePlant;
import com.ashveil.farming.Sapling;
import com.ashveil.objects.*;
import com.ashveil.world.*;
import com.ashveil.world.area.AreaID;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;

import java.lang.constant.DynamicCallSiteDesc;

public class WorldRenderer {

    private final EnemyRenderer enemyRenderer;
    private ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private final Matrix4 screenProjection;

    private Texture farmTileTexture;
    private Texture wheatTexture;
    private TextureRegion[] wheatStages;
    private Texture saplingTexture;
    private TextureRegion[] saplingStages;

    private Texture chestTexture;
    private Texture fenceTexture;

    private TextureRegion chestSealedRegion;
    private TextureRegion chestClosedRegion;
    private TextureRegion chestOpenedRegion;

    private TextureRegion normalFenceRegion;
    private TextureRegion thornFenceRegion;

    private Texture briarSnareTexture;
    private TextureRegion briarSnareRegion;

    private Texture hollowcapTexture;
    private TextureRegion hollowcapRegion;

    public WorldRenderer(TileMap tileMap) {
        shapeRenderer = new ShapeRenderer();
        setTileMap(tileMap);

        enemyRenderer = new EnemyRenderer();

        spriteBatch = new SpriteBatch();
        setTextures();
        screenProjection = new Matrix4().setToOrtho2D(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
    }

    public void setTileMap(TileMap tileMap){
        if (tileMap == null) throw new IllegalArgumentException("Tile map cannot be null");
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();

        tiledMap = tileMap.getTiledMap();
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, Config.SCALE);
    }

    public void setTextures(){
        farmTileTexture = new Texture("textures/farming/farm_tile.png");
        wheatTexture = new Texture("textures/farming/wheat_stages.png");
        TextureRegion[][] regions = TextureRegion.split(wheatTexture, 16, 16);
        wheatStages = regions[0];
        saplingTexture = new Texture("textures/farming/tree_stages.png");
        regions = TextureRegion.split(saplingTexture, 16, 32);
        saplingStages = regions[0];

        chestTexture = new Texture("textures/objects/chest_states.png");
        chestTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextureRegion[][] chestRegions = TextureRegion.split(chestTexture, 88, 88);
        chestSealedRegion = chestRegions[0][0];
        chestClosedRegion = chestRegions[0][1];
        chestOpenedRegion = chestRegions[0][2];

        fenceTexture = new Texture("textures/objects/fences.png");
        fenceTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextureRegion[][] fenceRegions = TextureRegion.split(fenceTexture, 32, 32);
        normalFenceRegion = fenceRegions[0][0];
        thornFenceRegion = fenceRegions[0][1];

        briarSnareTexture = new Texture("textures/objects/briar_snare.png");
        briarSnareTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        briarSnareRegion = new TextureRegion(briarSnareTexture);

        hollowcapTexture = new Texture("textures/objects/hollowcap.png");
        hollowcapTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        hollowcapRegion = new TextureRegion(hollowcapTexture);
    }

    public void render(World world, CameraController cameraController) {
        tiledMapRenderer.setView(cameraController.camera);
        tiledMapRenderer.render();

        spriteBatch.setProjectionMatrix(cameraController.camera.combined);
        spriteBatch.begin();
        drawFarmingTextures(world);
        for (Enemy enemy : world.getEnemies()) {
            enemyRenderer.render(enemy, spriteBatch);
        }
        for (Projectile projectile : world.getProjectileSystem().getProjectiles()){
            enemyRenderer.renderProjectile(projectile, spriteBatch);
        }

        drawObjectTextures(world);

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

        for (DestructibleObject o : world.getDestructibleObjects()) {

            if (o.getType() == DestructibleObjectType.CHEST || o.getType().isFence()
                || o.getType() == DestructibleObjectType.BRIAR_SNARE || o.getType() == DestructibleObjectType.HOLLOWCAP) continue;

            switch(o.getType()){
                case TREE -> {
                    shapeRenderer.setColor(Color.GREEN);
                    break;
                }
                case ROCK -> {
                    shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
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

        drawEnvironmentOverlay(world);
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

    public void drawChestTexture(World world, Chest chest){
        ChestVisualState visualState = getChestVisualState(world, chest);
        TextureRegion region = getChestTextureRegion(visualState);

        float drawWidth = Config.CHEST_WORLD_SIZE * Config.SCALE;
        float drawHeight = Config.CHEST_WORLD_SIZE * Config.SCALE;

        float drawX = chest.getX() * Config.SCALE + (Config.TILE_DRAW_SIZE - drawWidth) / 2f;
        float drawY = chest.getY() * Config.SCALE;

        spriteBatch.draw(region, drawX, drawY, drawWidth, drawHeight);
    }

    public ChestVisualState getChestVisualState(World world, Chest chest){
        if (world.getActiveChest() == chest){
            return ChestVisualState.OPEN;
        }

        if (chest.getKind() == ChestKind.GUARDIAN && !world.getProgressionState().isWardCleared(world.getCurrentAreaId())){
            return ChestVisualState.SEALED;
        }

        return ChestVisualState.CLOSED;
    }

    public TextureRegion getChestTextureRegion(ChestVisualState visualState){
        return switch (visualState){
            case SEALED -> chestSealedRegion;
            case CLOSED -> chestClosedRegion;
            case OPEN -> chestOpenedRegion;
        };
    }

    public void drawObjectTextures(World world){
        for (DestructibleObject object : world.getDestructibleObjects()){
            switch (object.getType()){
                case CHEST -> drawChestTexture(world, (Chest) object);
                case FENCE -> drawObjectTexture(normalFenceRegion, object, Config.TILE_DRAW_SIZE, Config.TILE_DRAW_SIZE);
                case THORN_FENCE -> drawObjectTexture(thornFenceRegion, object, Config.TILE_DRAW_SIZE, Config.TILE_DRAW_SIZE);
                case BRIAR_SNARE -> drawObjectTexture(briarSnareRegion,object, Config.TILE_DRAW_SIZE, Config.TILE_DRAW_SIZE);
                case HOLLOWCAP -> drawObjectTexture(hollowcapRegion, object, Config.TILE_DRAW_SIZE, Config.TILE_DRAW_SIZE * 2);
                default -> {}
            }
        }
    }

    private void drawObjectTexture(TextureRegion region, DestructibleObject object, float drawWidth, float drawHeight){
        spriteBatch.draw(region, object.getX() * Config.SCALE, object.getY() * Config.SCALE, drawWidth, drawHeight);
    }

    private void drawEnvironmentOverlay(World world){
        DayPhase dayPhase = world.getDayNightCycle().getDayPhase();

        boolean veilscar = world.getCurrentAreaId() == AreaID.VEILSCAR_PASSAGE;
        boolean guardianNight = world.isGuardianEncounterActive();

        if (!veilscar && !guardianNight && dayPhase == DayPhase.DAY) return;

        shapeRenderer.setProjectionMatrix(screenProjection);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (veilscar) {
            shapeRenderer.setColor(0.45f, 0.02f, 0.03f, 0.08f);
            shapeRenderer.rect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        }
        else if (guardianNight) shapeRenderer.setColor(0.04f, 0.06f, 0.22f, 0.50f);
        else if (dayPhase == DayPhase.DUSK){
            float alpha = 0.25f * world.getDayNightCycle().getPhaseProgress();
            shapeRenderer.setColor(0.76f, 0.32f, 0.10f, alpha);
        }
        else shapeRenderer.setColor(0.04f, 0.06f, 0.22f, 0.50f);

        shapeRenderer.rect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public int getMapWidthInTiles() {return tiledMap.getProperties().get("width", Integer.class);}
    public int getMapHeightInTiles() {return tiledMap.getProperties().get("height", Integer.class);}
    public float getMapRenderWidth() {return getMapWidthInTiles() * Config.TILE_DRAW_SIZE;}
    public float getMapRenderHeight() {return getMapHeightInTiles() * Config.TILE_DRAW_SIZE;}

    public void dispose() {
        shapeRenderer.dispose();
        tiledMapRenderer.dispose();
        spriteBatch.dispose();
        enemyRenderer.dispose();

        farmTileTexture.dispose();
        wheatTexture.dispose();
        saplingTexture.dispose();

        chestTexture.dispose();
        fenceTexture.dispose();
        briarSnareTexture.dispose();
        hollowcapTexture.dispose();
    }
}
