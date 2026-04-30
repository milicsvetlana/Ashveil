package com.ashveil.screens;

import com.ashveil.Config;
import com.ashveil.GameApp;
import com.ashveil.rendering.HudRenderer;
import com.ashveil.rendering.WorldRenderer;
import com.ashveil.world.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {

    private GameApp game;
    private SpriteBatch batch;
    private WorldRenderer worldRenderer;
    private HudRenderer hudRenderer;
    private World world;
    private CameraController cameraController;
    private boolean craftingOpen;

    public GameScreen(GameApp game){
        this.game = game;
        batch = new SpriteBatch();
        worldRenderer = new WorldRenderer();
        cameraController = new CameraController();
        world = new World();
        hudRenderer = new HudRenderer();
        craftingOpen = false;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) craftingOpen = !craftingOpen;

        if (craftingOpen && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mx = Gdx.input.getX();
            float my = Config.SCREEN_HEIGHT - Gdx.input.getY();
            var clicked = hudRenderer.getCategoryAtClick(mx, my);
            if (clicked != null) hudRenderer.setSelectedCategory(clicked);
        }

        world.update(delta);
        if (world.getPlayer().isDead()){
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }
        cameraController.update(world.getPlayer().getX() * Config.SCALE, world.getPlayer().getY() * Config.SCALE);
        worldRenderer.render(world, cameraController);
        hudRenderer.render(world.getPlayer(), world.getDayNightCycle(), craftingOpen, world.getRecipes());
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        hudRenderer.dispose();
    }

    @Override public void resize(int i, int i1) {}
    @Override public void show(){}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
