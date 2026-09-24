package com.ashveil.ui.scroll;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

public class ScrollUi extends Stack implements Disposable {

    private final Texture parchmentTexture;
    private final Label titleLabel;
    private final Label bodyLabel;

    public ScrollUi(Skin skin){
        if (skin == null) throw new IllegalArgumentException("Skin cannot be null.");

        parchmentTexture = new Texture(Gdx.files.internal("ui/scroll/scroll-background.png"));
        parchmentTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        Image parchmentImage = new Image(parchmentTexture);

        titleLabel = new Label("", skin);
        titleLabel.setAlignment(Align.center);
        titleLabel.setFontScale(1.5f);

        bodyLabel = new Label("", skin);
        bodyLabel.setWrap(true);
        bodyLabel.setAlignment(Align.topLeft);
        bodyLabel.setFontScale(1.5f);

        Table content = new Table();

        content.pad(160f, 150f, 100f, 150f);
        content.top();
        content.add(titleLabel).growX().padBottom(45f);
        content.row();
        content.add(bodyLabel).growX().top();

        add(parchmentImage);
        add(content);
    }

    public void showScroll(String title, String body){
        titleLabel.setText(title);
        bodyLabel.setText(body);
    }

    @Override
    public void dispose() {

    }
}
