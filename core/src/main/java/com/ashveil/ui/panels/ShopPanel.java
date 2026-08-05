package com.ashveil.ui.panels;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ShopPanel extends MenuPanel {
    public ShopPanel(Skin skin) {
        super(skin);
        add(new Label("Shop", skin));
    }
}
