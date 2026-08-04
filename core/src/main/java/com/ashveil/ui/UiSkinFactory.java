package com.ashveil.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

//na jednom mestu pravi privremeni ui izgled
//kasnije, kad budemo imali teksture i fontove, menjacemo ovu klasu ili iz nje ucitavati pravi skin
//layout i skin logika ostaju isti
public final class UiSkinFactory {
    private UiSkinFactory(){}

    public static Skin create(){
        Skin skin = new Skin();
        //PRIVREMENO
        Pixmap backgroundPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        backgroundPixmap.setColor(0.08f, 0.08f, 0.1f, 0.95f);
        backgroundPixmap.fill();

        Texture backgroundTexture = new Texture(backgroundPixmap);
        backgroundPixmap.dispose();

        skin.add("menu-background", backgroundTexture);
        skin.add("menu-background-texture", backgroundTexture);
        //PRIVREMENO

        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();

        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;
        buttonStyle.downFontColor = Color.GRAY;
        buttonStyle.disabledFontColor = Color.RED;

        skin.add("default", buttonStyle);

        return skin;
    }
}
