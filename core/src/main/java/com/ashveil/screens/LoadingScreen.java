package com.ashveil.screens;

import com.ashveil.Config;
import com.ashveil.ui.UiSkinFactory;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LoadingScreen implements Screen {
    private final Stage stage;
    private final Skin skin;
    private final Table root;

    private final Runnable onFinished;

    private LoadingState state;
    private float stateTimer;
    private float totalElapsed;
    private float fadeAlpha;

    private final Texture logoTexture;
    private final Image logo;

    private boolean finished;

    public LoadingScreen(Runnable onFinished){
        if (onFinished == null) throw new IllegalArgumentException("Finished action cannot be null.");

        this.onFinished = onFinished;

        stage = new Stage(new ScreenViewport());
        skin = UiSkinFactory.create();

        logoTexture = new Texture("ui/logo-and-text.png");
        logo = new Image(logoTexture);

        root = new Table();
        root.setFillParent(true);
        root.center();

        state = LoadingState.FADE_IN;
        stateTimer = 0f;
        totalElapsed = 0f;
        fadeAlpha = 0f;

        finished = false;

        buildUi();
    }

    private void buildUi(){
        logo.setScaling(Scaling.fit);

        float logoWidth = 420f;
        float logoHeight = logoWidth * logoTexture.getHeight() / logoTexture.getWidth();
        Label loadingLabel = new Label("Loading...", skin);

        root.add(logo).size(logoWidth, logoHeight).padBottom(100f);
        root.row();
        root.add(loadingLabel);

        stage.addActor(root);
    }

    private void updateLoading(float delta){
        stateTimer += delta;
        totalElapsed += delta;

        switch(state){
            case FADE_IN -> {updateFadeIn();}
            case WAITING -> {updateWaiting();}
            case FADE_OUT -> {updateFadeOut();}
        }

        root.getColor().a = fadeAlpha;
    }

    private void updateFadeIn(){
        fadeAlpha = stateTimer / Config.LOADING_FADE_IN_DURATION;
        if (fadeAlpha >= 1f){
            fadeAlpha = 1f;
            state = LoadingState.WAITING;
            stateTimer = 0f;
        }
    }

    private void updateWaiting(){
        float fadeOutStartTime = Config.LOADING_MIN_VISIBLE_DURATION - Config.LOADING_FADE_OUT_DURATION;

        if (totalElapsed >= fadeOutStartTime){
            state = LoadingState.FADE_OUT;
            stateTimer = 0f;
        }
    }

    private void updateFadeOut(){
        float progress = stateTimer / Config.LOADING_FADE_OUT_DURATION;
        fadeAlpha = 1f - progress;

        if (fadeAlpha <= 0f){
            fadeAlpha = 0f;
            finished = true;
        }
    }


    @Override
    public void render (float delta){
        ScreenUtils.clear(0.02f, 0.02f, 0.03f, 1f);
        updateLoading(delta);
        if (finished){
            onFinished.run();
            return;
        }
        stage.act(delta);
        stage.draw();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
        logoTexture.dispose();
    }

}












