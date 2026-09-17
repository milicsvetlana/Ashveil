package com.ashveil.settings;

public class GameSettings {
    private float masterVolume;
    private float musicVolume;
    private float sfxVolume;
    private boolean fullscreen;
    private String language;

    public GameSettings(float masterVolume, float musicVolume, float sfxVolume, boolean fullscreen, String language){
        this.masterVolume = masterVolume;
        this.musicVolume = musicVolume;
        this.sfxVolume = sfxVolume;
        this.fullscreen = fullscreen;
        this.language = language;
    }

    public float getMasterVolume() {return masterVolume;}
    public float getMusicVolume() {return musicVolume;}
    public float getSfxVolume() {return sfxVolume;}
    public boolean isFullscreen() {return fullscreen;}
    public String getLanguage() {return language;}
}
