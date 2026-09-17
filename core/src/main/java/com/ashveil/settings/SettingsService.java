package com.ashveil.settings;

import com.ashveil.Config;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Preferences;

public class SettingsService {
    private static final String PREFERENCES_NAME = "ashveil-settings";
    private static final String MASTER_VOLUME = "masterVolume";
    private static final String MUSIC_VOLUME = "musicVolume";
    private static final String SFX_VOLUME = "sfxVolume";
    private static final String FULLSCREEN = "fullscreen";
    private static final String LANGUAGE = "language";

    private final Preferences preferences;
    private GameSettings currentSettings;

    public SettingsService(){
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        currentSettings = load();
    }

    private GameSettings load(){
        float masterVolume = preferences.getFloat(MASTER_VOLUME, 100f);
        float musicVolume = preferences.getFloat(MUSIC_VOLUME, 100f);
        float sfcVolume = preferences.getFloat(SFX_VOLUME, 100f);
        boolean fullscreen = preferences.getBoolean(FULLSCREEN, false);
        String language = preferences.getString(LANGUAGE, "English");

        return new GameSettings(masterVolume, musicVolume, sfcVolume, fullscreen, language);
    }

    public void applyAndSave(GameSettings settings){
        if (settings == null) throw new IllegalArgumentException("Settings cannot be null.");

        preferences.putFloat(MASTER_VOLUME, settings.getMasterVolume());
        preferences.putFloat(MUSIC_VOLUME, settings.getMusicVolume());
        preferences.putFloat(SFX_VOLUME, settings.getSfxVolume());
        preferences.putBoolean(FULLSCREEN, settings.isFullscreen());
        preferences.putString(LANGUAGE, settings.getLanguage());

        preferences.flush(); //smesta upisi promene na disk
        currentSettings = settings;
        applyDisplaySettings();
    }

    private void applyDisplaySettings(){
        if (currentSettings.isFullscreen()){
            Graphics.Monitor monitor = Gdx.graphics.getMonitor();
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode(monitor);
            Gdx.graphics.setUndecorated(true);
            // +1 na visinu je namerno, NE bag: Windows DWM tretira borderless prozor
            // koji je tačno iste rezolucije kao monitor kao "fullscreen optimized" prozor,
            // što ume da zaključa frame rate na 60Hz čim prozor dobije fokus (GLFW/NVIDIA bug).
            // Jedan pixel viška van vidljive oblasti izbegava taj poseban path.
            Gdx.graphics.setWindowedMode(mode.width, mode.height + 1);
        }
        else {
            Gdx.graphics.setUndecorated(false);
            Gdx.graphics.setWindowedMode(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        }
    }

    public void applyCurrentSettings(){
        applyDisplaySettings();
    }

    public GameSettings getCurrentSettings(){return currentSettings;}

}
