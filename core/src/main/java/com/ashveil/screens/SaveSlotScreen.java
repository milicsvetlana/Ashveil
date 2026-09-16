package com.ashveil.screens;

import com.ashveil.GameApp;
import com.ashveil.save.SaveSlotInfo;
import com.ashveil.save.SaveSlotStatus;
import com.ashveil.ui.save.SaveSlotCard;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SaveSlotScreen implements Screen {
    private final GameApp game;
    private final Stage stage;
    private final Skin skin;
    private final Texture backgroundTexture;

    public SaveSlotScreen(GameApp game){
        if (game == null) throw new IllegalArgumentException("Game cannot be null.");
        this.game = game;

        stage = new Stage(new ScreenViewport());
        skin = game.getUiSkin();
        backgroundTexture = new Texture("ui/save-slots/save-slots-background.png");

        buildBackground();
        buildUi();
    }

    private void buildBackground(){
        Image background = new Image(backgroundTexture);
        background.setFillParent(true);
        background.setScaling(Scaling.fill);
        stage.addActor(background);
    }

    private void buildUi(){
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("SELECT SAVE", skin);
        TextButton backButton = new TextButton("Back", skin, "save-slot-action");

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                game.showMainMenu();
            }
        });

        Table slotsTable = new Table();

        for (int slot = 1; slot <= 3; slot++){
            SaveSlotInfo slotInfo = game.getSaveService().getSlotInfo(slot);
            SaveSlotCard slotCard = new SaveSlotCard(skin, slotInfo);

            slotsTable.add(slotCard).width(720f).height(247f).padBottom(slot < 3 ? 8f : 0f);
            if (slot < 3) slotsTable.row();
        }

        root.add(title).padBottom(30f);
        root.row();
        root.add(slotsTable);
        root.row();
        root.add(backButton).width(180f).height(50f).padTop(16f);

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
        backgroundTexture.dispose();
    }
}
