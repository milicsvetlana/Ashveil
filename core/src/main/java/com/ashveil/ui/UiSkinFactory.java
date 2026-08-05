package com.ashveil.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

//na jednom mestu pravi privremeni ui izgled
//kasnije, kad budemo imali teksture i fontove, menjacemo ovu klasu ili iz nje ucitavati pravi skin
//layout i skin logika ostaju isti
public final class UiSkinFactory {
    private UiSkinFactory(){}

    public static Skin create(){
        Skin skin = new Skin();

        addSolidTexture(skin, "menu-background", new Color(0.08f, 0.08f, 0.1f, 0.95f));
        addSolidTexture(skin, "inventory-panel-background", new Color(0.11f, 0.11f, 0.14f, 1f));
        addSolidTexture(skin, "inventory-slot", new Color(0.18f, 0.18f, 0.22f, 1f));
        addSolidTexture(skin, "hotbar-slot", new Color(0.30f, 0.24f, 0.13f, 1f));
        addSolidTexture(skin, "item-placeholder", Color.WHITE);
        addSolidTexture(skin, "inventory-slot-selected", new Color(0.55f, 0.45f, 0.15f, 1f));
        addSolidTexture(skin, "inventory-slot-picked", new Color(0.85f, 0.45f, 0.10f, 1f));
        addSolidTexture(skin, "durability-bar-background", new Color(Color.WHITE));
        addSolidTexture(skin, "durability-bar-fill", new Color(0.55f, 0.20f, 0.10f, 1f));

        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();

        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.LIGHT_GRAY;
        buttonStyle.disabledFontColor = Color.RED;
        buttonStyle.checkedFontColor = Color.YELLOW;

        skin.add("default", buttonStyle);

        return skin;
    }

    private static void addSolidTexture(Skin skin, String name, Color color){
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        skin.add(name, texture);
    }

}
