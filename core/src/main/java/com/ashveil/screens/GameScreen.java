package com.ashveil.screens;

import com.ashveil.Config;
import com.ashveil.GameApp;
import com.ashveil.rendering.HudRenderer;
import com.ashveil.rendering.WorldRenderer;
import com.ashveil.input.PlayerInput;
import com.ashveil.input.KeyBindings;
import com.ashveil.targeting.TargetMode;
import com.ashveil.targeting.TileTargetingSystem;
import com.ashveil.ui.GameMenuUi;
import com.ashveil.ui.GameOverlay;
import com.ashveil.ui.PauseMenuUi;
import com.ashveil.ui.UiSkinFactory;
import com.ashveil.ui.chest.ChestUI;
import com.ashveil.world.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen implements Screen {

    private GameApp game;
    private WorldRenderer worldRenderer;
    private HudRenderer hudRenderer;
    private GameMenuUi gameMenuUi;
    private World world;
    private CameraController cameraController;
    private GameOverlay activeOverlay;
    private KeyBindings keyBindings;
    private DeathTransitionState deathTransitionState;
    private float deathFadeAlpha;
    private ShapeRenderer fadeRenderer;
    private TileTargetingSystem tileTargetingSystem;
    private Stage overlayStage;
    private ChestUI chestUi;
    private PauseMenuUi pauseMenuUi;
    private Skin uiSkin;

    public GameScreen(GameApp game){
        this.game = game;
        world = new World();
        worldRenderer = new WorldRenderer(world.getTileMap());
        cameraController = new CameraController();
        hudRenderer = new HudRenderer();
        uiSkin = UiSkinFactory.create();
        gameMenuUi = new GameMenuUi(uiSkin, world.getAvailableRecipes(), world, world.getPlayer().getInventory());
        activeOverlay = GameOverlay.NONE;
        keyBindings = new KeyBindings();
        deathTransitionState = DeathTransitionState.NONE;
        deathFadeAlpha = 0;
        fadeRenderer = new ShapeRenderer();
        tileTargetingSystem = new TileTargetingSystem(cameraController, world.getTileMap());
        overlayStage = new Stage(new ScreenViewport());
        chestUi = null;
        //prosledjujemo closepause kao runnable callback. ne sluzi za novu nit, vec samo prosledjuje akciju
        //koja pausemenuui moze kasnije pozvati
        pauseMenuUi = new PauseMenuUi(uiSkin, this::closePause);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);

        handleCancelBackInput();
        tileTargetingSystem.update();

        if (Gdx.input.isKeyJustPressed(keyBindings.getToggleOverlayKey())) {
            toggleMenu();
        }

        PlayerInput playerInput;

        if (activeOverlay == GameOverlay.NONE) playerInput = readPlayerInput();
        else playerInput = getNeutralPlayerInput();

        if (deathTransitionState != DeathTransitionState.NONE) updateDeathTransition(delta);
        else if (activeOverlay == GameOverlay.PAUSE) overlayStage.act(delta);
        else {
            world.update(delta, playerInput);

            if (world.getPlayer().isDead()) startDeathTransition();
            else {
                if (activeOverlay == GameOverlay.MENU) {
                    handleMenuInput();
                    gameMenuUi.act(delta);
                }
                else if (activeOverlay == GameOverlay.CHEST) {
                    handleChestInput();
                    overlayStage.act(delta);
                }
                else if (activeOverlay == GameOverlay.NONE) {
                    if (world.getActiveChest() != null) openChest();
                    else handleTargetActionInput();
                }

                cameraController.update(world.getPlayer().getCenterX() * Config.SCALE,
                                        world.getPlayer().getCenterY() * Config.SCALE,
                                               worldRenderer.getMapRenderWidth(),
                                               worldRenderer.getMapRenderHeight(), delta);
            }
        }

        worldRenderer.render(world, cameraController);

        if (world.getTargetMode() != TargetMode.NONE){
            boolean targetValid = world.isCurrentTargetValid(tileTargetingSystem.getTileX(), tileTargetingSystem.getTileY(),
                                                           tileTargetingSystem.getWorldX(), tileTargetingSystem.getWorldY());
            worldRenderer.renderTargetPreview(cameraController, tileTargetingSystem.getWorldX(),
                                              tileTargetingSystem.getWorldY(), targetValid);
        }

        hudRenderer.render(world.getPlayer(), world.getDayNightCycle());

        if (activeOverlay == GameOverlay.MENU) gameMenuUi.draw();
        if (activeOverlay == GameOverlay.CHEST || activeOverlay == GameOverlay.PAUSE) overlayStage.draw();
        if (deathTransitionState != DeathTransitionState.NONE) renderDeathFade();
    }

    private void toggleMenu(){
        if (activeOverlay == GameOverlay.MENU) activeOverlay = GameOverlay.NONE;
        else if (activeOverlay == GameOverlay.NONE) activeOverlay = GameOverlay.MENU;

        if (activeOverlay == GameOverlay.MENU) {
            gameMenuUi.onOpen();
            Gdx.input.setInputProcessor(gameMenuUi.getStage());
            world.cancelTargeting();
        } else {
            gameMenuUi.onClose();
            Gdx.input.setInputProcessor(null);
        }
    }

    private void closeChest(){
        if (activeOverlay != GameOverlay.CHEST) return;
        overlayStage.clear();
        chestUi = null;
        world.closeChest();
        activeOverlay = GameOverlay.NONE;
        Gdx.input.setInputProcessor(null);
    }

    private void openPause(){
        if (activeOverlay != GameOverlay.NONE) return;
        overlayStage.clear();
        Table overlayRoot = new Table();
        overlayRoot.setFillParent(true);
        overlayRoot.add(pauseMenuUi);

        overlayStage.addActor(overlayRoot);
        activeOverlay = GameOverlay.PAUSE;
        Gdx.input.setInputProcessor(overlayStage);

        world.cancelTargeting();
    }

    private void closePause(){
        if (activeOverlay != GameOverlay.PAUSE) return;

        overlayStage.clear();
        activeOverlay = GameOverlay.NONE;
        Gdx.input.setInputProcessor(null);
    }

    private PlayerInput readPlayerInput(){
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(keyBindings.getMoveUpKey())) moveY += 1f;
        if (Gdx.input.isKeyPressed(keyBindings.getMoveDownKey())) moveY -= 1f;
        if (Gdx.input.isKeyPressed(keyBindings.getMoveLeftKey())) moveX -= 1f;
        if (Gdx.input.isKeyPressed(keyBindings.getMoveRightKey())) moveX += 1f;

        boolean primaryActionPressed = Gdx.input.isKeyJustPressed(keyBindings.getPrimaryActionKey());
        boolean interactPressed = Gdx.input.isKeyJustPressed(keyBindings.getInteractKey());
        boolean useItemPressed = Gdx.input.isKeyJustPressed(keyBindings.getUseItemKey());
        boolean dropItemPressed = Gdx.input.isKeyJustPressed(keyBindings.getDropItemKey());

        boolean controlPressed = Gdx.input.isKeyPressed(keyBindings.getDropWholeStackModifierKey());
        boolean dropWholeStack = dropItemPressed && controlPressed;

        boolean dashPressed = Gdx.input.isKeyJustPressed(keyBindings.getDashKey());
        int selectedHotbarSlot = -1;

        for (int i = 0; i < keyBindings.getHotbarSize(); i++) {
            if (Gdx.input.isKeyJustPressed(keyBindings.getHotbarKey(i))) {
                selectedHotbarSlot = i;
                break;
            }
        }

        return new PlayerInput(moveX, moveY, primaryActionPressed, interactPressed, useItemPressed, dropItemPressed, dropWholeStack,
            dashPressed, selectedHotbarSlot);
    }
    //input koji saljemo worldu dok smo u inventory/chest meniju, da bi svet i dalje radio ali Player ne bi
    //mogao da se seta, udara i sl.
    private PlayerInput getNeutralPlayerInput(){
        return new PlayerInput(0f, 0f, false, false, false,
            false, false, false, -1);
    }

    private void handleMenuInput(){
        if (Gdx.input.isKeyJustPressed(keyBindings.getPreviousMenuTabKey())) gameMenuUi.showPreviousTab();
        if (Gdx.input.isKeyJustPressed(keyBindings.getNextMenuTabKey())) gameMenuUi.showNextTab();

        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuUpKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveUpKey())) {
            gameMenuUi.moveSelectionUp();
        }
        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuDownKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveDownKey())) {
            gameMenuUi.moveSelectionDown();
        }
        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuLeftKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveLeftKey())) {
            gameMenuUi.moveSelectionLeft();
        }
        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuRightKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveRightKey())) {
            gameMenuUi.moveSelectionRight();
        }

        if (Gdx.input.isKeyJustPressed(keyBindings.getPrimaryActionKey())) gameMenuUi.confirmSelection();
    }

    private void handleChestInput(){
        if (Gdx.input.isKeyJustPressed(keyBindings.getInteractKey())) closeChest();

        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuUpKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveUpKey()))
            chestUi.moveSelectionUp();

        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuDownKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveDownKey()))
            chestUi.moveSelectionDown();

        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuLeftKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveLeftKey()))
            chestUi.moveSelectionLeft();

        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuRightKey()) || Gdx.input.isKeyJustPressed(keyBindings.getMoveRightKey()))
            chestUi.moveSelectionRight();

        if (Gdx.input.isKeyJustPressed(keyBindings.getPrimaryActionKey()))chestUi.confirmSelection();
    }

    private void handleCancelBackInput(){
        if (!Gdx.input.isKeyJustPressed(keyBindings.getCancelBackKey())) return;

        if (activeOverlay == GameOverlay.MENU){
            toggleMenu();
            return;
        }

        if (activeOverlay == GameOverlay.CHEST){
            closeChest();
            return;
        }

        if (activeOverlay == GameOverlay.PAUSE){
            closePause();
            return;
        }

        if (world.getTargetMode() != TargetMode.NONE){
            world.cancelTargeting();
        }

        if (activeOverlay == GameOverlay.NONE) openPause();
    }

    private void handleTargetActionInput(){
        if (!Gdx.input.isButtonJustPressed(keyBindings.getTargetActionButton())) return;
        if (world.getTargetMode() == TargetMode.NONE) return;
        world.handleTargetAction(tileTargetingSystem.getTileX(), tileTargetingSystem.getTileY(), tileTargetingSystem.getWorldX(), tileTargetingSystem.getWorldY());
    }

    @Override public void resize(int i, int i1) {
        hudRenderer.resize(i, i1);
        gameMenuUi.resize(i, i1);
        overlayStage.getViewport().update(i, i1, true);
    }

    private void startDeathTransition(){
        deathTransitionState = DeathTransitionState.FADING_OUT;
        deathFadeAlpha = 0f;
        if (activeOverlay == GameOverlay.CHEST) closeChest();
        else if (activeOverlay == GameOverlay.MENU){
            gameMenuUi.onClose();
            activeOverlay = GameOverlay.NONE;
            Gdx.input.setInputProcessor(null);
        }
        world.cancelTargeting();
    }

    private void updateDeathTransition(float delta){
        if (deathTransitionState == DeathTransitionState.FADING_OUT){
            deathFadeAlpha += delta / Config.DEATH_FADE_DURATION;

            if (deathFadeAlpha >= 1f){
                deathFadeAlpha = 1f;
                world.respawnPlayer();
                deathTransitionState = DeathTransitionState.FADING_IN;
            }
        }
        else if (deathTransitionState == DeathTransitionState.FADING_IN){
            deathFadeAlpha -= delta / Config.DEATH_FADE_DURATION;

            if (deathFadeAlpha <= 0){
                deathFadeAlpha = 0;
                deathTransitionState = DeathTransitionState.NONE;
            }
        }
    }

    private void renderDeathFade(){
        fadeRenderer.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT));
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        fadeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        fadeRenderer.setColor(0f, 0f, 0f, deathFadeAlpha);
        fadeRenderer.rect(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        fadeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void openChest(){
        if (activeOverlay != GameOverlay.NONE) return;
        if (world.getActiveChest() == null) return;
        chestUi = new ChestUI(uiSkin, world.getPlayer().getInventory(), world.getActiveChest().getChestInventory());

        overlayStage.clear();

        Table overlayRoot = new Table();
        overlayRoot.setFillParent(true);
        overlayRoot.add(chestUi);

        overlayStage.addActor(overlayRoot);
        activeOverlay = GameOverlay.CHEST;
        Gdx.input.setInputProcessor(overlayStage);
        world.cancelTargeting();
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        hudRenderer.dispose();
        world.dispose();
        if (Gdx.input.getInputProcessor() == gameMenuUi.getStage()) {
            Gdx.input.setInputProcessor(null);
        }
        gameMenuUi.dispose();
        fadeRenderer.dispose();
        overlayStage.dispose();
    }

    @Override public void show(){}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
