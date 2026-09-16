package com.ashveil.screens;

import com.ashveil.GameApp;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import javax.swing.*;

public class CharacterCreationScreen implements Screen {
    private final GameApp game;
    private final int saveSlot;
    private final Stage stage;
    private final Skin skin;
    private final Texture backgroundTexture;

    public CharacterCreationScreen(GameApp game, int saveSlot){
        if (game == null) throw new IllegalArgumentException("Game cannot be null.");

        this.game = game;
        this.saveSlot = saveSlot;

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

        Image dimOverlay = new Image(skin.getDrawable("screen-dim"));
        dimOverlay.setFillParent(true);
        dimOverlay.setColor(0f, 0f, 0f, 0.42f);

        stage.addActor(dimOverlay);
    }

    private void buildUi(){
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("CREATE CHARACTER", skin);
        title.setFontScale(1.8f);

        Image titleDivider = new Image(skin.getDrawable("save-slots-title-divider"));
        titleDivider.setScaling(Scaling.fill);

        Table titleBlock = new Table();
        titleBlock.add(title);
        titleBlock.row();
        titleBlock.add(titleDivider).width(360f).height(20f).padTop(10f);

        Stack portraitStack = new Stack();

        Image portraitIcon = new Image(skin.getDrawable("save-slot-empty-icon"));
        portraitIcon.setScaling(Scaling.fit);

        Table portraitLayer = new Table();
        portraitLayer.add(portraitIcon).size(104f, 104f);

        Image portraitFrame = new Image(skin.getDrawable("save-slot-portrait-frame"));
        portraitStack.add(portraitLayer);
        portraitStack.add(portraitFrame);

        Label nameLabel = new Label("Name", skin);
        nameLabel.setFontScale(1.15f);

        TextField nameField = new TextField("", skin, "character-name");
        nameField.setMessageText("Enter name...");
        nameField.setMaxLength(30);

        Table characterInfo = new Table();
        characterInfo.left();
        characterInfo.add(nameLabel).left().padBottom(8f);
        characterInfo.row();
        characterInfo.add(nameField).width(420f).height(58f).padRight(20f);

        Table panelContent = new Table();
        panelContent.add(portraitStack).size(140f, 140f).padRight(28f);
        panelContent.add(characterInfo).left();

        Image panelBackground = new Image(skin.getDrawable("save-slot-card-normal"));

        Stack characterPanel = new Stack();
        characterPanel.add(panelBackground);

        Table panelWrapper = new Table();
        panelWrapper.add(panelContent);
        characterPanel.add(panelWrapper);

        TextButton createButton = new TextButton("Create", skin, "save-slot-action");
        TextButton backButton = new TextButton("Back", skin, "save-slot-action");

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                game.showSaveSlots();
            }
        });

        Label errorLabel = new Label("", skin);

        createButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                String characterName = nameField.getText().trim();
                if (characterName.isEmpty()){
                    errorLabel.setText("Enter a character name.");
                    return;
                }

                errorLabel.setText("");
                game.startNewGame(saveSlot, characterName);
            }
        });

        root.add(titleBlock).padBottom(28f);
        root.row();
        root.add(characterPanel).width(720f).height(247f).padBottom(14f);
        root.row();
        root.add(errorLabel).height(26f).padBottom(6f);
        root.row();
        root.add(createButton).width(220f).height(58f).padBottom(12f);
        root.row();
        root.add(backButton).width(180f).height(50f);

        stage.addActor(root);
    }


    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    @Override public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.showSaveSlots();
            return;
        }

        ScreenUtils.clear(0.05f, 0.05f, 0.07f, 1f);

        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
        if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null);
    }
    @Override public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
