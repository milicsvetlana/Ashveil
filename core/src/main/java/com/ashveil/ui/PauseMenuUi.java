package com.ashveil.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;

public class PauseMenuUi extends Window {
    public PauseMenuUi(Skin skin, Runnable onContinue, Runnable onOptions, Runnable onMainMenu){
        super("", skin);

        Label titleLabel = new Label("PAUSED", skin);
        titleLabel.setFontScale(1.5f);

        Image titleDivider = new Image(skin.getDrawable("save-slots-title-divider"));
        titleDivider.setScaling(Scaling.fill);

        Table titleBlock = new Table();
        titleBlock.add(titleLabel).center();
        titleBlock.row();
        titleBlock.add(titleDivider).width(220f).height(18f).padTop(6f).center();

        TextButton continueButton = new TextButton("Continue", skin, "save-slot-action");
        continueButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                //poziva callback koji je gamescreen prosledio u konstruktoru
                //run se ovde izvrsava normalno, sinhrono, u istoj niti
                onContinue.run();
            }
        });

        TextButton optionsButton = new TextButton("Options", skin, "save-slot-action");
        optionsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                onOptions.run();
            }
        });

        TextButton mainMenuButton = new TextButton("Exit to Main Menu", skin, "save-slot-action");
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                onMainMenu.run();
            }
        });

        setBackground(skin.getDrawable("settings-panel-background"));
        pad(28f, 45f, 35f, 45f);

        add(titleBlock).center().padTop(10f).padBottom(48f);
        row();
        add(continueButton).width(300f).height(55f).padTop(25f).padBottom(12f);
        row();
        add(optionsButton).width(300f).height(55f).padBottom(12f);
        row();
        add(mainMenuButton).width(300f).height(55f);

        pack();
    }
}
