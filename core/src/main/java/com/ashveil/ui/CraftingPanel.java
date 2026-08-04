package com.ashveil.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

//sadrzace kasnije listu recepata, kategorije kao obicne naslove, details panel, crafting dugme
public class CraftingPanel extends Table {
    public CraftingPanel(Skin skin) {
        super(skin);
        add(new Label("Crafting", skin));
    }
}
