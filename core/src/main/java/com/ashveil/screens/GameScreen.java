package com.ashveil.screens;

import com.ashveil.Config;
import com.ashveil.GameApp;
import com.ashveil.rendering.HudRenderer;
import com.ashveil.rendering.WorldRenderer;
import com.ashveil.input.PlayerInput;
import com.ashveil.input.KeyBindings;
import com.ashveil.ui.GameMenuUi;
import com.ashveil.world.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {

    private GameApp game;
    private WorldRenderer worldRenderer;
    private HudRenderer hudRenderer;
    private GameMenuUi gameMenuUi;
    private World world;
    private CameraController cameraController;
    private boolean menuOpen;
    private KeyBindings keyBindings;
    private DeathTransitionState deathTransitionState;
    private float deathFadeAlpha;
    private ShapeRenderer fadeRenderer;

    public GameScreen(GameApp game){
        this.game = game;
        world = new World();
        worldRenderer = new WorldRenderer(world.getTileMap());
        cameraController = new CameraController();
        hudRenderer = new HudRenderer();
        gameMenuUi = new GameMenuUi(world.getAvailableRecipes(), world, world.getPlayer().getInventory());
        menuOpen = false;
        keyBindings = new KeyBindings();
        deathTransitionState = DeathTransitionState.NONE;
        deathFadeAlpha = 0;
        fadeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) { // delta je vreme proteklo od prethodnog frejma, u sekundama (za 60FPS je 0.016s)
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);

        PlayerInput playerInput = readPlayerInput();

        if (Gdx.input.isKeyJustPressed(keyBindings.getToggleOverlayKey())) toggleMenu();

        if (deathTransitionState != DeathTransitionState.NONE) updateDeathTransition(delta);
        else if (menuOpen) {
            handleMenuInput();
            gameMenuUi.act(delta);
        }
        else {
            world.update(delta, playerInput);

            if (world.getPlayer().isDead()) startDeathTransition();

            cameraController.update(world.getPlayer().getCenterX() * Config.SCALE,
                             world.getPlayer().getCenterY() * Config.SCALE,
                                    worldRenderer.getMapRenderWidth(), worldRenderer.getMapRenderHeight(), delta);
        }

        worldRenderer.render(world, cameraController);
        hudRenderer.render(world.getPlayer(), world.getDayNightCycle());

        if (menuOpen) gameMenuUi.draw();

        if (deathTransitionState != DeathTransitionState.NONE) renderDeathFade();
    }

    private void toggleMenu(){
        menuOpen = !menuOpen;

        if (menuOpen) {
            gameMenuUi.onOpen();
            Gdx.input.setInputProcessor(gameMenuUi.getStage());
        } else {
            gameMenuUi.onClose();
            Gdx.input.setInputProcessor(null);
        }
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

        if (Gdx.input.isKeyJustPressed(keyBindings.getMenuConfirmKey())) gameMenuUi.confirmSelection();
    }

    @Override public void resize(int i, int i1) {
        hudRenderer.resize(i, i1);
        gameMenuUi.resize(i, i1);
    }

    private void startDeathTransition(){
        deathTransitionState = DeathTransitionState.FADING_OUT;
        deathFadeAlpha = 0f;
        if (menuOpen){
            menuOpen = false;
            Gdx.input.setInputProcessor(null);
        }
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
    }

    @Override public void show(){}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
