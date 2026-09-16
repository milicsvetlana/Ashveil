package com.ashveil.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

//na jednom mestu pravi privremeni ui izgled
//kasnije, kad budemo imali teksture i fontove, menjacemo ovu klasu ili iz nje ucitavati pravi skin
//layout i skin logika ostaju isti
public final class UiSkinFactory {
    private UiSkinFactory(){}

    private static final String DEFAULT_FONT = "default-font";

    public static Skin create(){
        Skin skin = new Skin();

        addPlaceholderTextures(skin);
        addFonts(skin);
        addDefaultStyles(skin);
        addMainMenuStyles(skin);
        addSaveSlotStyles(skin);

        return skin;
    }

    private static void addPlaceholderTextures(Skin skin){
        addSolidTexture(skin, "menu-background", new Color(0.08f, 0.08f, 0.1f, 0.95f));
        addSolidTexture(skin, "inventory-panel-background", new Color(0.11f, 0.11f, 0.14f, 1f));
        addSolidTexture(skin, "inventory-slot", new Color(0.18f, 0.18f, 0.22f, 1f));
        addSolidTexture(skin, "hotbar-slot", new Color(0.30f, 0.24f, 0.13f, 1f));
        addSolidTexture(skin, "item-placeholder", Color.WHITE);
        addSolidTexture(skin, "inventory-slot-selected", new Color(0.55f, 0.45f, 0.15f, 1f));
        addSolidTexture(skin, "inventory-slot-picked", new Color(0.85f, 0.45f, 0.10f, 1f));
        addSolidTexture(skin, "durability-bar-background", new Color(Color.WHITE));
        addSolidTexture(skin, "durability-bar-fill", new Color(0.55f, 0.20f, 0.10f, 1f));
    }

    private static void addSolidTexture(Skin skin, String name, Color color){
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        skin.add(name, texture);
    }

    private static void addFonts(Skin skin){
        skin.add(DEFAULT_FONT, createDefaultFont());
    }

    private static BitmapFont createDefaultFont(){
        return new BitmapFont();
    }

    private static void addDefaultStyles(Skin skin){
        BitmapFont font = skin.getFont(DEFAULT_FONT);

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.LIGHT_GRAY;
        buttonStyle.disabledFontColor = Color.RED;
        buttonStyle.checkedFontColor = Color.YELLOW;
        skin.add("default", buttonStyle);

        Window.WindowStyle windowStyle = new Window.WindowStyle(font, Color.WHITE, skin.getDrawable("menu-background"));
        skin.add("default", windowStyle);
    }

    private static void addTextButtonStyle(Skin skin, String styleName, String assetPrefix){
        BitmapFont font = skin.getFont(DEFAULT_FONT);

        String normalName = styleName + "-normal";
        String hoverName = styleName + "-hover";
        String pressedName = styleName + "-pressed";
        String disabledName = styleName + "-disabled";

        addUiTexture(skin, normalName, assetPrefix + "-normal.png");
        addUiTexture(skin, hoverName, assetPrefix + "-hover.png");
        addUiTexture(skin, pressedName, assetPrefix + "-pressed.png");
        addUiTexture(skin, disabledName, assetPrefix + "-disabled.png");

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();

        style.up = skin.getDrawable(normalName);
        style.over = skin.getDrawable(hoverName);
        style.down = skin.getDrawable(pressedName);
        style.disabled = skin.getDrawable(disabledName);

        style.font = font;
        style.fontColor = Color.WHITE;

        skin.add(styleName, style);
    }

    private static void addUiTexture(Skin skin, String name, String path){
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        skin.add(name, texture);
    }

    private static void addMainMenuStyles(Skin skin){
        addTextButtonStyle(skin, "main-menu", "ui/main-menu/buttons/button");
    }

    private static void addSaveSlotStyles(Skin skin){
        addUiTexture(skin, "save-slot-card", "ui/save-slots/slot-card.png");
        addUiTexture(skin, "save-slot-card-unavailable", "ui/save-slots/slot-card-unavailable.png");
        addTextButtonStyle(skin, "save-slot-action", "ui/save-slots/action-button");
        addUiTexture(skin, "save-slot-portrait-frame", "ui/save-slots/portrait-frame.png");
        addUiTexture(skin, "save-slot-portrait-placeholder", "ui/save-slots/temp-image.png");
    }
}
