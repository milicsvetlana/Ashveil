package com.ashveil.screens;

import com.ashveil.Config;
import com.ashveil.GameApp;
import com.ashveil.items.crafting.CraftingCategory;
import com.ashveil.rendering.HudRenderer;
import com.ashveil.rendering.WorldRenderer;
import com.ashveil.input.PlayerInput;
import com.ashveil.world.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {

    private GameApp game;
    private WorldRenderer worldRenderer;
    private HudRenderer hudRenderer;
    private World world;
    private CameraController cameraController;
    private boolean menuOpen;

    public GameScreen(GameApp game){
        this.game = game;
        worldRenderer = new WorldRenderer();
        cameraController = new CameraController();
        world = new World();
        hudRenderer = new HudRenderer();
        menuOpen = false;
    }

    @Override
    public void render(float delta) { // delta je vreme proteklo od prethodnog frejma, u sekundama (za 60FPS je 0.016s)
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);
        PlayerInput playerInput = readPlayerInput();
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) menuOpen = !menuOpen;

        if (menuOpen) {
            handleMenuInput();
        }
        else {
            world.update(delta, playerInput);
            if (world.getPlayer().isDead()){
                game.setScreen(new GameOverScreen(game));
                dispose();
                return;
            }
            cameraController.update(world.getPlayer().getCenterX() * Config.SCALE, world.getPlayer().getCenterY() * Config.SCALE, delta);
        }
        worldRenderer.render(world, cameraController);
        hudRenderer.render(world.getPlayer(), world.getDayNightCycle(), menuOpen, world.getRecipes());
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        hudRenderer.dispose();
    }

    private PlayerInput readPlayerInput(){
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX += 1f;

        boolean primaryActionPressed = Gdx.input.isKeyJustPressed(Input.Keys.K);
        boolean interactPressed = Gdx.input.isKeyJustPressed(Input.Keys.E);
        boolean useItemPressed = Gdx.input.isKeyJustPressed(Input.Keys.F);
        boolean dropItemPressed = Gdx.input.isKeyJustPressed(Input.Keys.Q);

        boolean controlPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        boolean dropWholeStack = dropItemPressed && controlPressed;

        boolean dashPressed = Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT);
        int selectedHotbarSlot = -1;

        int[] hotbarKeys = {
            Input.Keys.NUM_1,
            Input.Keys.NUM_2,
            Input.Keys.NUM_3,
            Input.Keys.NUM_4,
            Input.Keys.NUM_5
        };

        for (int i = 0; i < hotbarKeys.length; i++) {
            if (Gdx.input.isKeyJustPressed(hotbarKeys[i])) {
                selectedHotbarSlot = i;
                break;
            }
        }

        return new PlayerInput(moveX, moveY, primaryActionPressed, interactPressed, useItemPressed, dropItemPressed, dropWholeStack,
            dashPressed, selectedHotbarSlot);
    }

    private void handleMenuInput(){
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;
        CraftingCategory clicked = hudRenderer.getCategoryAtScreenClick(Gdx.input.getX(), Gdx.input.getY());
        if (clicked != null) hudRenderer.setSelectedCategory(clicked);
    }

    @Override public void resize(int i, int i1) {
        hudRenderer.resize(i, i1);
    }
    @Override public void show(){}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
