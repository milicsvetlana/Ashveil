package com.ashveil;

import com.ashveil.localization.LocalizationService;
import com.ashveil.save.SaveService;
import com.ashveil.save.data.SaveData;
import com.ashveil.screens.*;
import com.ashveil.settings.SettingsService;
import com.ashveil.ui.UiSkinFactory;
import com.ashveil.world.World;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.concurrent.Future;

public class GameApp extends Game {
    private SaveService saveService;
    private Skin uiSkin;
    private SettingsService settingsService;
    private LocalizationService localizationService;
    private GameScreen settingsReturnGameScreen;

    @Override
    public void create() {
        saveService = new SaveService();
        settingsService = new SettingsService();

        localizationService = new LocalizationService(settingsService.getCurrentSettings().getLanguage());

        settingsService.applyCurrentSettings();
        uiSkin = UiSkinFactory.create();

        showStartupLoading();
    }

    public void startNewGame(int slot, String characterName){
        switchScreen(new LoadingScreen(() -> finishStartNewGame(slot, characterName), uiSkin));
    }

    private void finishStartNewGame(int slot, String characterName){
        World world = new World();
        world.getPlayer().setCharacterName(characterName);
        saveService.requestSave(slot, world);
        switchScreen(new GameScreen(this, slot, world));
    }

    public void loadGame(int slot){
        Future<SaveData> loadTask = saveService.requestLoad(slot);
        switchScreen(new LoadingScreen(loadTask::isDone, () -> finishLoadGame(slot, loadTask), uiSkin));
    }

    private void finishLoadGame(int slot, Future<SaveData> loadTask){
        World world = saveService.completeLoad(loadTask);
        if (world == null){
            showSaveSlots();
            return;
        }
        switchScreen(new GameScreen(this, slot, world));
    }

    public void showSettings(){
        switchScreen(new SettingsScreen(this));
    }

    private void switchScreen(Screen newScreen){
        Screen currentScreen = getScreen();
        setScreen(newScreen);
        if (currentScreen != null) currentScreen.dispose();
    }

    public void showCharacterCreation(int slot){
        switchScreen(new CharacterCreationScreen(this, slot));
    }

    public void showSettingsFromPause(GameScreen gameScreen){
        if (gameScreen == null) throw new IllegalArgumentException("Game screen cannot be null.");

        settingsReturnGameScreen = gameScreen;
        setScreen(new SettingsScreen(this));
    }

    public void closeSettings(){
        if (settingsReturnGameScreen == null){
            showMainMenu();
            return;
        }

        Screen settingsScreen = getScreen();
        GameScreen gameScreen = settingsReturnGameScreen;

        settingsReturnGameScreen = null;

        setScreen(gameScreen);
        if (settingsScreen != null) settingsScreen.dispose();
    }

    public SaveService getSaveService() {return saveService;}
    public Skin getUiSkin() {return uiSkin;}

    public void showMainMenu(){
        switchScreen(new MainMenuScreen(this));
    }
    public void showMainMenuLoading(){
        switchScreen(new LoadingScreen(this::showMainMenu, uiSkin));
    }

    public void showSaveSlots(){
        switchScreen(new SaveSlotScreen(this));
    }

    public void showStartupLoading(){
        switchScreen(new LoadingScreen(this::showMainMenu, uiSkin));
    }

    public void quit() {
        Gdx.app.exit();
    }

    public void dispose(){
        Screen currentScreen = getScreen();
        if (currentScreen instanceof GameScreen gameScreen) gameScreen.saveGame();
        if (currentScreen != null) currentScreen.dispose();
        saveService.shutdownAndWait();
        if (uiSkin != null) uiSkin.dispose();
    }

    public SettingsService getSettingsService() {return settingsService;}
    public LocalizationService getLocalizationService() {return localizationService;}
}
