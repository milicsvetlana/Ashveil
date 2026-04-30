package com.ashveil;

import com.ashveil.screens.GameScreen;
import com.badlogic.gdx.Game;

public class GameApp extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }
}
