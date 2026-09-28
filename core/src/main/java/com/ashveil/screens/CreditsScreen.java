package com.ashveil.screens;

import com.ashveil.Config;
import com.ashveil.GameApp;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CreditsScreen implements Screen {
    private static final float SCROLL_SPEED = 30f;
    private static final String EXTRA_CHARACTERS = "ĆćČčŠšŽžĐđ";

    private final GameApp game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final GlyphLayout layout;

    private final BitmapFont titleFont;
    private final BitmapFont headingFont;
    private final BitmapFont textFont;

    private float scrollOffset;

    public CreditsScreen(GameApp game){
        if (game == null) throw new IllegalArgumentException("Game cannot be null.");

        this.game = game;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT, camera);
        layout = new GlyphLayout();

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/ashveil-title.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter titleParameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        titleParameter.size = 64;
        titleParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARACTERS;
        titleFont = generator.generateFont(titleParameter);

        FreeTypeFontGenerator.FreeTypeFontParameter headingParameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        headingParameter.size = 36;
        headingParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARACTERS;
        headingFont = generator.generateFont(headingParameter);

        FreeTypeFontGenerator.FreeTypeFontParameter textParameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        textParameter.size = 28;
        textParameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARACTERS;
        textFont = generator.generateFont(textParameter);

        generator.dispose();

        scrollOffset = 0f;
    }

    @Override
    public void render(float delta){
        handleInput();

        scrollOffset += SCROLL_SPEED * delta;

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        float y = -150f + scrollOffset;

        drawCentered(titleFont, "ASHVEIL", y);
        y -= 120f;

        drawCentered(headingFont, "Created By", y);
        y -= 65f;

        drawCentered(textFont, "Svetlana Milić (Ceca)", y);
        y -= 120f;

        drawCentered(headingFont, "Game Design", y);
        y -= 65f;

        drawCentered(textFont, "Svetlana Milić", y);
        y -= 120f;

        drawCentered(headingFont, "Programming", y);
        y -= 65f;

        drawCentered(textFont, "Svetlana Milić", y);
        y -= 120f;

        drawCentered(headingFont, "Art & UI", y);
        y -= 65f;

        drawCentered(textFont, "Svetlana Milić", y);
        y -= 120f;

        drawCentered(headingFont, "Game Tester", y);
        y -= 65f;

        drawCentered(textFont, "Đorđe Rajčić", y);
        y -= 120f;

        drawCentered(headingFont, "Special Thanks", y);
        y -= 65f;

        drawCentered(textFont, "Đorđe Rajčić", y);
        y -= 180f;

        drawCentered(titleFont, "Thank you for playing.", y);

        batch.end();
    }

    private void handleInput(){
        if (!Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            && !Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) return;

        game.showMainMenuLoading();
    }

    private void drawCentered(BitmapFont font, String text, float y){
        layout.setText(font, text);

        float x = (Config.SCREEN_WIDTH - layout.width) / 2f;

        font.draw(batch, text, x, y);
    }

    @Override
    public void resize(int width, int height){
        viewport.update(width, height, true);
    }

    @Override
    public void dispose(){
        batch.dispose();
        titleFont.dispose();
        headingFont.dispose();
        textFont.dispose();
    }

    @Override
    public void show(){
        viewport.apply();
    }

    @Override
    public void pause(){}

    @Override
    public void resume(){}

    @Override
    public void hide(){}
}
