package com.ashveil.screens;

import com.ashveil.Config;
import com.ashveil.GameApp;
import com.ashveil.rendering.HudRenderer;
import com.ashveil.rendering.WorldRenderer;
import com.ashveil.input.PlayerInput;
import com.ashveil.world.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.*;

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

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) menuOpen = !menuOpen;

        if (menuOpen && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mx = Gdx.input.getX();
            float my = Config.SCREEN_HEIGHT - Gdx.input.getY();
            var clicked = hudRenderer.getCategoryAtClick(mx, my);
            if (clicked != null) hudRenderer.setSelectedCategory(clicked);
        }
        PlayerInput playerInput = readPlayerInput();
        world.update(delta, playerInput);
        if (world.getPlayer().isDead()){
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }
        cameraController.update(world.getPlayer().getCenterX() * Config.SCALE, world.getPlayer().getCenterY() * Config.SCALE, delta);
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

        return new PlayerInput(moveX, moveY, primaryActionPressed, interactPressed);
    }

    @Override public void resize(int i, int i1) {}
    @Override public void show(){}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
