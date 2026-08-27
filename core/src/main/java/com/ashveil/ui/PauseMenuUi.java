package com.ashveil.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class PauseMenuUi extends Window {
    public PauseMenuUi(Skin skin, Runnable onContinue){
        super("Paused", skin);

        TextButton continueButton = new TextButton("Continue", skin);
        continueButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                //poziva callback koji je gamescreen prosledio u konstruktoru
                //run se ovde izvrsava normalno, sinhrono, u istoj niti
                onContinue.run();
            }
        });

        add(continueButton).width(100).pad(30);
        pack();
    }
}
