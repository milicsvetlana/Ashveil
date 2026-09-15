package com.ashveil.screens;

import com.ashveil.GameApp;
import com.ashveil.ui.UiSkinFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen {
    private final GameApp game;
    private final Stage stage;
    private final Skin skin;
    private final Texture backgroundTexture;

    float buttonWidth = 400f;
    float buttonHeight = 62f;
    float buttonGap = 15f;

    public MainMenuScreen(GameApp game) {
        if (game == null) throw new IllegalArgumentException("Game cannot be null.");
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = UiSkinFactory.create();
        backgroundTexture = new Texture("ui/main-menu/main-menu-background.png");
        buildBackground();
        buildMenu();
    }

    private void buildBackground(){
        Image background = new Image(backgroundTexture);

        background.setFillParent(true);
        background.setScaling(Scaling.fill);

        stage.addActor(background);
    }

    private void buildMenu(){
        Table menuTable = new Table();
        menuTable.setFillParent(true);
        menuTable.bottom();
        menuTable.padBottom(100f);

        TextButton singlePlayerButton = new TextButton("Singleplayer", skin, "main-menu");
        TextButton multiPlayerButton = new TextButton("Multiplayer", skin, "main-menu");
        TextButton optionsButton = new TextButton("Options", skin, "main-menu");
        TextButton quitGameButton = new TextButton("Quit Game", skin, "main-menu");

        singlePlayerButton.getLabel().setAlignment(Align.center);
        multiPlayerButton.getLabel().setAlignment(Align.center);
        optionsButton.getLabel().setAlignment(Align.center);
        quitGameButton.getLabel().setAlignment(Align.center);

        multiPlayerButton.setDisabled(true);
        optionsButton.setDisabled(true);

        singlePlayerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                game.showSaveSlots();
            }
        });

        quitGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                game.quit();
            }
        });

        menuTable.add(singlePlayerButton).width(buttonWidth).height(buttonHeight).padBottom(buttonGap);
        menuTable.row();

        menuTable.add(multiPlayerButton).width(buttonWidth).height(buttonHeight).padBottom(buttonGap);
        menuTable.row();

        menuTable.add(optionsButton).width(buttonWidth).height(buttonHeight).padBottom(buttonGap);
        menuTable.row();

        menuTable.add(quitGameButton).width(buttonWidth).height(buttonHeight);

        stage.addActor(menuTable);
    }

    @Override
    public void show(){
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta){
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    public void hide(){
        if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null);
    }

    public void dispose(){
        stage.dispose();
        skin.dispose();
        backgroundTexture.dispose();
    }

}
