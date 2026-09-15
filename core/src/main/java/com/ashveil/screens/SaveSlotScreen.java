package com.ashveil.screens;

import com.ashveil.GameApp;
import com.ashveil.ui.UiSkinFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import org.w3c.dom.Text;

import javax.lang.model.util.ElementScanner6;

public class SaveSlotScreen implements Screen {
    private final GameApp game;
    private final Stage stage;
    private final Skin skin;

    public SaveSlotScreen(GameApp game){
        if (game == null) throw new IllegalArgumentException("Game cannot be null.");
        this.game = game;

        stage = new Stage(new ScreenViewport());
        skin = UiSkinFactory.create();

        buildUi();
    }

    private void buildUi(){
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("SELECT SAVE", skin);
        TextButton backButton = new TextButton("Back", skin);

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                game.showMainMenu();
            }
        });

        root.add(title).padBottom(30f);
        root.row();

        root.add(backButton).width(180f).height(50f);
        stage.addActor(root);
    }

    @Override
    public void render(float delta){
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.showMainMenu();
            return;
        }

        ScreenUtils.clear(0.05f, 0.05f, 0.07f, 1f);

        stage.act(delta);
        stage.draw();
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void hide() {
        if (Gdx.input.getInputProcessor() == stage){
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
