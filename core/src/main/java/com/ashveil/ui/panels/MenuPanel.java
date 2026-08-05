package com.ashveil.ui.panels;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public abstract class MenuPanel extends Table {

    protected MenuPanel(Skin skin) {super(skin);}
    public void refresh() {}
    public void onShow() {refresh();}
    public void onHide() {}
}
