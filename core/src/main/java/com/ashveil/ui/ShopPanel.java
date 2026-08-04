package com.ashveil.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class ShopPanel extends Table {
    public ShopPanel(Skin skin) {
        super(skin);
        add(new Label("Shop", skin));
    }
}
