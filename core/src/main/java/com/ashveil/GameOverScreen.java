package com.ashveil;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen implements Screen {
    private GameApp game;
    private SpriteBatch batch;
    private BitmapFont font;

    public GameOverScreen(GameApp game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont(); // default LibGDX font
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.begin();
        font.draw(batch, "GAME OVER",
            Config.SCREEN_WIDTH / 2f - 50,
            Config.SCREEN_HEIGHT / 2f);
        font.draw(batch, "Pritisni ENTER za novi pokusaj",
            Config.SCREEN_WIDTH / 2f - 120,
            Config.SCREEN_HEIGHT / 2f - 40);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
