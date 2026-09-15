package com.ashveil;

import com.ashveil.save.SaveService;
import com.ashveil.save.SaveSlotStatus;
import com.ashveil.screens.GameScreen;
import com.ashveil.screens.LoadingScreen;
import com.ashveil.screens.MainMenuScreen;
import com.ashveil.screens.SaveSlotScreen;
import com.ashveil.world.World;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class GameApp extends Game {
    private SaveService saveService;

    @Override
    public void create() {
        saveService = new SaveService();
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

    public boolean openSlot(int slot){
        SaveSlotStatus status = saveService.getSlotStatus(slot);

        if (status == SaveSlotStatus.EMPTY){
            startNewGame(slot);
            return true;
        }
        if (status == SaveSlotStatus.VALID) return loadGame(slot);
        return false;
    }

    public SaveService getSaveService() {return saveService;}

    public void showMainMenu(){
        switchScreen(new MainMenuScreen(this));
    }

    public void showSaveSlots(){
        switchScreen(new SaveSlotScreen(this));
    }

    public void showStartupLoading(){
        switchScreen(new LoadingScreen(this::showMainMenu));
    }

    public void quit() {
        Gdx.app.exit();
    }

    public void dispose(){
        Screen currentScreen = getScreen();
        if (currentScreen instanceof GameScreen gameScreen) gameScreen.saveGame();
        if (currentScreen != null) currentScreen.dispose();
        saveService.shutdownAndWait();
    }

}
