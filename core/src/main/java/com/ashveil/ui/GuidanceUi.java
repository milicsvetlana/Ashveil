package com.ashveil.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

public class GuidanceUi extends Table {
    private final Label messageLabel;
    private boolean hiding;
    private final Label controlHintLabel;

    public GuidanceUi(Skin skin){

        Texture parchmentTexture = skin.get("guidance-parchment", Texture.class);
        Texture portraitTexture = skin.get("ceca-portrait", Texture.class);

        setBackground(new TextureRegionDrawable(new TextureRegion(parchmentTexture)));

        setTransform(true);

        Image portraitImage = new Image(portraitTexture);
        Label nameLabel = new Label("Ceca", skin);

        messageLabel = new Label("", skin);
        messageLabel.setWrap(true);
        messageLabel.setAlignment(Align.left);

        controlHintLabel = new Label("", skin);
        controlHintLabel.setAlignment(Align.left);

        Label continueLabel = new Label("[ENTER]", skin);

        Table textTable = new Table();
        textTable.top().left().padTop(30f);
        textTable.add(nameLabel).left().padBottom(18f);
        textTable.row();
        textTable.add(messageLabel).width(650f).left().top().padBottom(8f);
        textTable.row();
        textTable.add(controlHintLabel).left().padTop(8f).padBottom(6f);
        textTable.row();
        textTable.add(continueLabel).right().padTop(4f);

        pad(26f, 105f, 24f, 40f);

        add(portraitImage).size(130f).top().padRight(18f);
        add(textTable).width(690f).top().left();

        hiding = false;
        setVisible(false);
    }

    public void showMessage(String message, String controlHint){
        if (message == null || message.isBlank()) return;

        clearActions();

        hiding = false;
        messageLabel.setText(message);
        controlHintLabel.setText(controlHint == null ? "" : controlHint);
        setVisible(true);
        setScale(0.97f);
        getColor().a = 0f;

        addAction(Actions.parallel(Actions.fadeIn(0.22f, Interpolation.fade),
                                   Actions.scaleTo(1f, 1f, 0.22f, Interpolation.sineOut)));
    }

    public void hideMessage(){
        if (!isVisible() || hiding) return;

        hiding = true;
        clearActions();

        addAction(
            Actions.sequence(Actions.parallel(Actions.fadeOut(0.16f, Interpolation.fade),
                                              Actions.scaleTo(0.98f, 0.98f, 0.16f, Interpolation.sineIn)),
                             Actions.visible(false), Actions.run(() -> {setScale(1f);
                                                                        getColor().a = 1f;
                                                                        hiding = false;})
            )
        );
    }

    public boolean isMessageVisible(){return isVisible() && !hiding;}

}
