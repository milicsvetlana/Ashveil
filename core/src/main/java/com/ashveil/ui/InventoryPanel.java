package com.ashveil.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

//prikazuje svih 20 slotova
public class InventoryPanel extends Table {

    public InventoryPanel(Skin skin) {
        super(skin);
        add(new Label("Inventory", skin));
    }
}
