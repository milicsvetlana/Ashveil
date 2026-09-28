package com.ashveil.rendering;

import com.ashveil.Config;
import com.ashveil.combat.Projectile;
import com.ashveil.entities.Player;
import com.ashveil.entities.enemies.Enemy;
import com.ashveil.farming.Crop;
import com.ashveil.farming.GrowablePlant;
import com.ashveil.farming.Sapling;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.ui.inventory.InventoryGridUi;
import com.ashveil.objects.*;
import com.ashveil.ui.inventory.ItemIconUi;
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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.lang.constant.DynamicCallSiteDesc;

public class WorldRenderer {

    private final EnemyRenderer enemyRenderer;
    private ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private final Matrix4 screenProjection;

    private Texture playerIdleTexture;
    private Texture swordOverlayTexture;

    private TextureRegion[] playerIdleRegions;
    private TextureRegion[] swordOverlayRegions;

    private Texture playerWalkTexture;
    private Texture playerWalk2Texture;

    private TextureRegion[] playerWalkRegions;
    private TextureRegion[] playerWalk2Regions;

    private float playerWalkTimer;

    private Texture decorationsTexture;

    private TextureRegion treeRegion1;
    private TextureRegion treeRegion2;
    private TextureRegion rockRegion;

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

    private Skin skin;

    public WorldRenderer(TileMap tileMap, Skin skin) {
        this.skin = skin;
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
        playerIdleTexture = new Texture("player/body_idle.png");
        playerIdleTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        playerIdleRegions = createPlayerRegions(playerIdleTexture);

        playerWalkTexture = new Texture("player/body_walk.png");
        playerWalkTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        playerWalkRegions = createPlayerRegions(playerWalkTexture);

        playerWalk2Texture = new Texture("player/body_walk_2.png");
        playerWalk2Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        playerWalk2Regions = createPlayerRegions(playerWalk2Texture);

        swordOverlayTexture = new Texture("player/sword_overlay.png");
        swordOverlayTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        playerIdleRegions = createPlayerRegions(playerIdleTexture);
        swordOverlayRegions = createPlayerRegions(swordOverlayTexture);

        decorationsTexture = new Texture("tilesets/Decorations/Decorations.png");
        decorationsTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        treeRegion1 = new TextureRegion(decorationsTexture, 0, 144, 80, 96);
        treeRegion2 = new TextureRegion(decorationsTexture, 160, 160, 48, 80);
        rockRegion = new TextureRegion(decorationsTexture, 224, 128, 32, 32);

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

        drawNaturalResourceTextures(world, false);
        drawFarmingTextures(world);
        drawGroundItems(world);

        for (Enemy enemy : world.getEnemies()) {
            enemyRenderer.render(enemy, spriteBatch);
        }

        for (Projectile projectile : world.getProjectileSystem().getProjectiles()) {
            enemyRenderer.renderProjectile(projectile, spriteBatch);
        }

        drawObjectTextures(world);
        drawPlayer(world.getPlayer());
        drawNaturalResourceTextures(world, true);

        spriteBatch.end();

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

    private int getPlayerDirectionIndex(Player player){
        return switch (player.getFacing()){
            case DOWN -> 0;
            case UP -> 1;
            case LEFT -> 2;
            case RIGHT -> 3;
        };
    }

    private void drawPlayer(Player player){
        int directionIndex = getPlayerDirectionIndex(player);

        TextureRegion bodyRegion = getPlayerBodyRegion(player, directionIndex);

        float drawHeight = Config.TILE_DRAW_SIZE * 2.5f;
        float drawWidth = drawHeight * bodyRegion.getRegionWidth() / bodyRegion.getRegionHeight();

        float drawX = player.getX() * Config.SCALE + (Config.TILE_DRAW_SIZE - drawWidth) / 2f;
        float drawY = player.getY() * Config.SCALE;

        spriteBatch.draw(bodyRegion, drawX, drawY, drawWidth, drawHeight);

        if (!hasSwordSelected(player)) return;

        TextureRegion swordRegion = swordOverlayRegions[directionIndex];
        spriteBatch.draw(swordRegion, drawX, drawY, drawWidth, drawHeight);
    }

    public void drawNaturalResourceTextures(World world, boolean foreground){
        float playerY = world.getPlayer().getY();

        for (DestructibleObject object : world.getDestructibleObjects()){
            TextureRegion region;

            if (object.getType() == DestructibleObjectType.TREE){
                region = getTreeRegion(object);
            }
            else if (object.getType() == DestructibleObjectType.ROCK){
                region = rockRegion;
            }
            else continue;

            boolean objectInFrontOfPlayer = object.getY() < playerY;
            if (objectInFrontOfPlayer != foreground) continue;

            float drawWidth = region.getRegionWidth() * Config.SCALE;
            float drawHeight = region.getRegionHeight() * Config.SCALE;

            float drawX = object.getX() * Config.SCALE + (Config.TILE_DRAW_SIZE - drawWidth) / 2f;
            float drawY = object.getY() * Config.SCALE;

            spriteBatch.draw(region, drawX, drawY, drawWidth, drawHeight);
        }
    }

    private TextureRegion[] createPlayerRegions(Texture texture){
        return new TextureRegion[]{
            new TextureRegion(texture, 0, 0, 439, 595),
            new TextureRegion(texture, 439, 0, 440, 595),
            new TextureRegion(texture, 879, 0, 439, 595),
            new TextureRegion(texture, 1318, 0, 440, 595)
        };
    }

    private TextureRegion getPlayerBodyRegion(Player player, int directionIndex){
        if (!player.isMoving()){
            playerWalkTimer = 0f;
            return playerIdleRegions[directionIndex];
        }

        playerWalkTimer += Gdx.graphics.getDeltaTime();

        float frameDuration = 0.12f;
        int frame = (int) (playerWalkTimer / frameDuration) % 4;

        return switch (frame){
            case 0 -> playerWalkRegions[directionIndex];
            case 1 -> playerIdleRegions[directionIndex];
            case 2 -> playerWalk2Regions[directionIndex];
            default -> playerIdleRegions[directionIndex];
        };
    }

    private boolean hasSwordSelected(Player player){
        ItemType itemType = player.getInventory().getItemTypeBySlot(player.getSelectedHotbarSlot());

        return itemType == ItemType.WOODEN_SWORD || itemType == ItemType.STONE_SWORD || itemType == ItemType.BLOODTHIRST_SWORD;
    }

    private TextureRegion getTreeRegion(DestructibleObject object){
        int tileX = (int) (object.getX() / Config.TILE_SIZE);
        int tileY = (int) (object.getY() / Config.TILE_SIZE);

        if ((tileX + tileY) % 2 == 0) return treeRegion1;

        return treeRegion2;
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
        boolean crimsonWarning = world.isCrimsonVeilWarning();
        boolean crimsonActive = world.isCrimsonVeilActive();
        boolean crimsonRecovery = world.isCrimsonVeilRecovery();

        if (!veilscar && !guardianNight && !crimsonWarning && !crimsonActive && !crimsonRecovery && dayPhase == DayPhase.DAY) return;

        shapeRenderer.setProjectionMatrix(screenProjection);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (guardianNight){
            shapeRenderer.setColor(0.04f, 0.06f, 0.22f, 0.50f);
        }
        else if (crimsonActive){
            shapeRenderer.setColor(0.28f, 0.035f, 0.16f, 0.52f);
        }
        else if (crimsonRecovery){
            shapeRenderer.setColor(0.18f, 0.04f, 0.18f, 0.48f);
        }
        else if (dayPhase == DayPhase.DUSK){
            float alpha = 0.25f * world.getDayNightCycle().getPhaseProgress();

            if (crimsonWarning) shapeRenderer.setColor(0.68f, 0.20f, 0.12f, alpha);
            else shapeRenderer.setColor(0.76f, 0.32f, 0.10f, alpha);
        }
        else if (dayPhase == DayPhase.NIGHT){
            shapeRenderer.setColor(0.04f, 0.06f, 0.22f, 0.50f);
        }
        else{
            shapeRenderer.setColor(0f, 0f, 0f, 0f);
        }

        shapeRenderer.rect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);

        if (veilscar){
            shapeRenderer.setColor(0.45f, 0.02f, 0.03f, 0.08f);
            shapeRenderer.rect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawGroundItems(World world){
        float size = Config.TILE_DRAW_SIZE;

        for (WorldItem item : world.getGroundItems()){Drawable drawable = ItemIconUi.getDrawable(skin, item.getType());

            float drawX = item.getX() * Config.SCALE;
            float drawY = item.getY() * Config.SCALE;

            drawable.draw(spriteBatch, drawX, drawY, size, size);
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
        enemyRenderer.dispose();

        playerIdleTexture.dispose();
        playerWalkTexture.dispose();
        playerWalk2Texture.dispose();
        swordOverlayTexture.dispose();

        decorationsTexture.dispose();
        farmTileTexture.dispose();
        wheatTexture.dispose();
        saplingTexture.dispose();

        chestTexture.dispose();
        fenceTexture.dispose();
        briarSnareTexture.dispose();
        hollowcapTexture.dispose();
    }
}
