package com.ashveil.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

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
        addSolidTexture(skin, "screen-dim", Color.WHITE);
        addCharacterCreationStyles(skin);
        addSettingsStyles(skin);

        skin.add("guidance-parchment", new Texture(Gdx.files.internal("ui/guidance/guidance-parchment.png")));
        Texture cecaPortrait = new Texture(Gdx.files.internal("ui/guidance/ceca-portrait.png"));
        cecaPortrait.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        skin.add("ceca-portrait", cecaPortrait);

        Texture swiftStepReward = new Texture(Gdx.files.internal("ui/rewards/swift-step-unlock.png"));
        swiftStepReward.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        skin.add("reward-swift-step", swiftStepReward);

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
        addUiTexture(skin, "save-slot-card-normal", "ui/save-slots/slot-card-normal.png");
        addUiTexture(skin, "save-slot-card-hover", "ui/save-slots/slot-card-hover.png");
        addUiTexture(skin, "save-slot-card-selected", "ui/save-slots/slot-card-selected.png");
        addUiTexture(skin, "save-slot-card-unavailable", "ui/save-slots/slot-card-unavailable.png");
        addTextButtonStyle(skin, "save-slot-action", "ui/save-slots/action-button");
        addUiTexture(skin, "save-slot-portrait-frame", "ui/save-slots/portrait-frame.png");
        addUiTexture(skin, "save-slots-title-divider", "ui/save-slots/save-slots-title-divider.png");
        addUiTexture(skin, "save-slot-empty-icon", "ui/save-slots/slot-card-empty-icon.png");
        addUiTexture(skin, "dialog-box", "ui/save-slots/dialog-box.png");
    }

    private static void addCharacterCreationStyles(Skin skin){
        addUiTexture(skin, "character-name-field", "ui/character-creation/character-name-field.png");
        TextField.TextFieldStyle nameFieldStyle = new TextField.TextFieldStyle();

        nameFieldStyle.font = skin.getFont("default-font");
        nameFieldStyle.fontColor = Color.WHITE;
        nameFieldStyle.messageFontColor = new Color(1f, 1f, 1f, 0.45f);
        Drawable fieldBackground = skin.newDrawable("character-name-field");
        if (fieldBackground instanceof BaseDrawable baseDrawable) {
            baseDrawable.setLeftWidth(22f);
            baseDrawable.setRightWidth(18f);
        }
        nameFieldStyle.background = fieldBackground;

        skin.add("character-name", nameFieldStyle, TextField.TextFieldStyle.class);
    }

    private static void addSettingsStyles(Skin skin){
        addUiTexture(skin, "settings-panel-background", "ui/settings-screen/settings-panel-background.png");
        addUiTexture(skin, "settings-slider-track", "ui/settings-screen/settings-slider-track.png");
        addUiTexture(skin, "settings-slider-knob", "ui/settings-screen/settings-slider-knob.png");
        addUiTexture(skin, "settings-toggle-off", "ui/settings-screen/settings-toggle-off.png");
        addUiTexture(skin, "settings-toggle-on", "ui/settings-screen/settings-toggle-on.png");
        addUiTexture(skin, "settings-dropdown", "ui/settings-screen/settings-dropdown.png");
        addUiTexture(skin, "settings-dropdown-popup", "ui/settings-screen/settings-dropdown-popup.png");
        addUiTexture(skin, "settings-dropdown-selection", "ui/settings-screen/settings-dropdown-selection.png");

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = sizedDrawable(skin, "settings-slider-track", 380f, 36f);
        sliderStyle.knob = sizedDrawable(skin, "settings-slider-knob", 40f, 40f);
        skin.add("settings-slider", sliderStyle, Slider.SliderStyle.class);

        Button.ButtonStyle toggleStyle = new Button.ButtonStyle();
        toggleStyle.up = sizedDrawable(skin, "settings-toggle-off", 110f, 53f);
        toggleStyle.checked = sizedDrawable(skin, "settings-toggle-on", 110f, 53f);
        skin.add("settings-toggle", toggleStyle, Button.ButtonStyle.class);

        BitmapFont font = skin.getFont(DEFAULT_FONT);
        Drawable dropdownBackground = sizedDrawable(skin, "settings-dropdown", 330f, 59f);

        if (dropdownBackground instanceof BaseDrawable baseDrawable){
            baseDrawable.setLeftWidth(40f);
            baseDrawable.setRightWidth(70f);
            baseDrawable.setTopHeight(8f);
            baseDrawable.setBottomHeight(8f);
        }

        Drawable popupBackground = sizedDrawable(skin, "settings-dropdown-popup", 330f, 110f);
        if (popupBackground instanceof BaseDrawable baseDrawable){
            baseDrawable.setLeftWidth(16f);
            baseDrawable.setRightWidth(16f);
            baseDrawable.setTopHeight(8f);
            baseDrawable.setBottomHeight(8f);
        }

        Drawable selection = sizedDrawable(skin, "settings-dropdown-selection", 300f, 48f);
        if (selection instanceof BaseDrawable baseDrawable){
            baseDrawable.setLeftWidth(35f);
            baseDrawable.setRightWidth(15f);
            baseDrawable.setTopHeight(7f);
            baseDrawable.setBottomHeight(7f);
        }

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = font;
        listStyle.fontColorUnselected = Color.WHITE;
        listStyle.fontColorSelected = Color.WHITE;
        listStyle.selection = selection;

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = popupBackground;

        SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
        selectBoxStyle.font = font;
        selectBoxStyle.fontColor = Color.WHITE;
        selectBoxStyle.background = dropdownBackground;
        selectBoxStyle.listStyle = listStyle;
        selectBoxStyle.scrollStyle = scrollStyle;

        skin.add("settings-language", selectBoxStyle, SelectBox.SelectBoxStyle.class);
    }

    private static Drawable sizedDrawable(Skin skin, String name, float width, float height){
        Drawable drawable = skin.newDrawable(name);
        if (drawable instanceof BaseDrawable baseDrawable){
            baseDrawable.setMinWidth(width);
            baseDrawable.setMinHeight(height);
        }
        return drawable;
    }
}
