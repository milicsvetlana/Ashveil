package com.ashveil.ui;

import com.badlogic.gdx.graphics.Color;
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

        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();

        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;
        buttonStyle.downFontColor = Color.GRAY;

        skin.add("default", buttonStyle);

        return skin;
    }
}
