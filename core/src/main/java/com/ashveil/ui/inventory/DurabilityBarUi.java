package com.ashveil.ui.inventory;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class DurabilityBarUi extends Table {

    private static final float BAR_WIDTH = 52f;
    private static final float BAR_HEIGHT = 6f;

    private final Cell<Image> fillCell;

    public DurabilityBarUi(Skin skin) {
        Image background = new Image(skin.getDrawable("durability-bar-background"));
        Image fill = new Image(skin.getDrawable("durability-bar-fill"));

        Table fillLayer = new Table();
        fillLayer.left();

        fillCell = fillLayer.add(fill).width(BAR_WIDTH).growY();

        Stack barStack = new Stack();
        barStack.add(background);
        barStack.add(fillLayer);

        bottom();
        add(barStack).width(BAR_WIDTH).height(BAR_HEIGHT).padBottom(5);
        setVisible(false);

        setTouchable(Touchable.disabled);
        barStack.setTouchable(Touchable.disabled);
        fillLayer.setTouchable(Touchable.disabled);
        background.setTouchable(Touchable.disabled);
        fill.setTouchable(Touchable.disabled);
    }

    public void setDurability(int currentDurability, int maxDurability) {
        if (maxDurability <= 0) {
            setVisible(false);
            return;
        }

        float ratio = (float) currentDurability / maxDurability;
        ratio = Math.max(0f, Math.min(1f, ratio));

        fillCell.width(BAR_WIDTH * ratio);
        fillCell.getTable().invalidateHierarchy();

        setVisible(true);
    }

    public void clear() {
        setVisible(false);
    }
}
