package com.ashveil;

import com.ashveil.save.SaveService;import com.ashveil.screens.GameScreen;
import com.ashveil.screens.LoadingScreen;
import com.ashveil.screens.MainMenuScreen;
import com.ashveil.screens.SaveSlotScreen;
import com.ashveil.ui.UiSkinFactory;
import com.ashveil.world.World;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class GameApp extends Game {
    private SaveService saveService;
    private Skin uiSkin;

    @Override
    public void create() {
        saveService = new SaveService();
        uiSkin = UiSkinFactory.create();

        showStartupLoading();
    }

    public void startNewGame(int slot){
        switchScreen(new GameScreen(this, slot));
    }

    public boolean loadGame(int slot){
        World world = saveService.loadWorld(slot);
        if (world == null) return false;
        switchScreen(new GameScreen(this, slot, world));
        return true;
    }

    private void switchScreen(Screen newScreen){
        Screen currentScreen = getScreen();
        setScreen(newScreen);
        if (currentScreen != null) currentScreen.dispose();
    }

    public SaveService getSaveService() {return saveService;}
    public Skin getUiSkin() {return uiSkin;}

    public void showMainMenu(){
        switchScreen(new MainMenuScreen(this));
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

}
