package com.ashveil.screens;

import com.ashveil.GameApp;
import com.ashveil.localization.LocalizationService;
import com.ashveil.settings.GameSettings;
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

public class SettingsScreen implements Screen {
    private final GameApp game;
    private final Stage stage;
    private final Skin skin;
    private final Texture backgroundTexture;
    private final LocalizationService i18n;

    public SettingsScreen(GameApp game){
        if (game == null) throw new IllegalArgumentException("Game cannot be null.");
        this.game = game;
        stage = new Stage(new ScreenViewport());
        skin = game.getUiSkin();
        i18n = game.getLocalizationService();

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
        GameSettings currentSettings = game.getSettingsService().getCurrentSettings();
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label(i18n.get("settings.title"), skin);
        title.setFontScale(1.8f);

        Image titleDivider = new Image(skin.getDrawable("save-slots-title-divider"));
        titleDivider.setScaling(Scaling.fill);

        Table titleBlock = new Table();
        titleBlock.add(title);
        titleBlock.row();
        titleBlock.add(titleDivider).width(300f).height(18f).padTop(8f);

        Slider masterVolume = new Slider(0f, 100f, 1f, false, skin, "settings-slider");
        Slider musicVolume = new Slider(0f, 100f, 1f, false, skin, "settings-slider");
        Slider sfxVolume = new Slider(0f, 100f, 1f, false, skin, "settings-slider");

        masterVolume.setValue(currentSettings.getMasterVolume());
        musicVolume.setValue(currentSettings.getMusicVolume());
        sfxVolume.setValue(currentSettings.getSfxVolume());

        Button fullscreenToggle = new Button(skin, "settings-toggle");
        fullscreenToggle.setChecked(currentSettings.isFullscreen());

        SelectBox<String> languageSelect = new SelectBox<>(skin, "settings-language");
        languageSelect.setItems("English", "Srpski");
        languageSelect.setSelected(currentSettings.getLanguage());

        TextButton configureButton = new TextButton(i18n.get("settings.configure"), skin, "save-slot-action");
        TextButton applyButton = new TextButton(i18n.get("common.apply"), skin, "save-slot-action");
        TextButton backButton = new TextButton(i18n.get("common.back"), skin, "save-slot-action");

        applyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                GameSettings newSettings = new GameSettings(masterVolume.getValue(), musicVolume.getValue(), sfxVolume.getValue(),
                                                            fullscreenToggle.isChecked(), languageSelect.getSelected());
                game.getSettingsService().applyAndSave(newSettings);
                game.getLocalizationService().setLanguage(newSettings.getLanguage());

                Gdx.app.postRunnable(SettingsScreen.this::rebuildUi);
            }
        });

        Table settingsContent = new Table();
        addSectionTitle(settingsContent, i18n.get("settings.audio"));

        addSliderRow(settingsContent, i18n.get("settings.masterVolume"), masterVolume);
        addSliderRow(settingsContent, i18n.get("settings.musicVolume"), musicVolume);
        addSliderRow(settingsContent, i18n.get("settings.sfxVolume"), sfxVolume);

        addSectionTitle(settingsContent,  i18n.get("settings.display"));
        addControlRow(settingsContent, i18n.get("settings.fullscreen"), fullscreenToggle, 125f, 60f);

        addSectionTitle(settingsContent, i18n.get("settings.language"));
        addControlRow(settingsContent, i18n.get("settings.languageLabel"), languageSelect, 380f, 64f);

        addSectionTitle(settingsContent, i18n.get("settings.controls"));
        addControlRow(settingsContent, i18n.get("settings.keyBindings"), configureButton, 200f, 50f);
        settingsContent.pad(60f, 100f, 60f, 100f);

        Stack settingsPanel = new Stack();
        Image panelBackground = new Image(skin.getDrawable("settings-panel-background"));
        settingsPanel.add(panelBackground);
        settingsPanel.add(settingsContent);

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.postRunnable(game::closeSettings);
            }
        });

        root.add(titleBlock).padBottom(18f);
        root.row();
        root.add(settingsPanel).width(980f).height(740f).padBottom(12f);
        root.row();

        Table actions = new Table();
        actions.add(applyButton).width(180f).height(50f).padRight(10f);
        actions.add(backButton).width(180f).height(50f);
        root.add(actions);

        stage.addActor(root);
    }

    public void rebuildUi(){
        stage.clear();
        buildBackground();
        buildUi();
    }

    private void addSectionTitle(Table table, String text){
        Label label = new Label(text, skin);
        label.setFontScale(1.2f);
        table.add(label).colspan(2).left().padTop(10f).padBottom(8f);
        table.row();
    }

    private void addSliderRow(Table table, String labelText, Slider slider){
        Label label = new Label(labelText, skin);
        label.setFontScale(1.08f);
        table.add(label).left().expandX().padBottom(12f);
        table.add(slider).width(380f).height(46f).right().padBottom(12f);
        table.row();
    }

    private void addControlRow(Table table, String labelText, Actor control, float width, float height){
        Label label = new Label(labelText, skin);
        table.add(label).left().expandX().padBottom(12f);
        table.add(control).width(width).height(height).right().padBottom(12f);
        table.row();
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    @Override public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.closeSettings();
            return;
        }

        ScreenUtils.clear(0.05f, 0.05f, 0.07f, 1f);
        stage.act(delta);
        stage.draw();
    }
    @Override public void resize(int width, int height){
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












