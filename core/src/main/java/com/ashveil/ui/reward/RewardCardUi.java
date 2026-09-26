package com.ashveil.ui.reward;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;

public class RewardCardUi extends Table {

    private static final float CARD_WIDTH = 360f;
    private static final float CARD_HEIGHT = 560f;

    private static final float DESCRIPTION_WIDTH = 270f;
    private static final float DESCRIPTION_BOTTOM_PADDING = 92f;

    private static final float DIM_ALPHA = 0.72f;
    private static final float DISPLAY_DURATION = 4f;

    private final Skin skin;

    private final Image dimOverlay;
    private final Image cardImage;
    private final Label descriptionLabel;
    private final Stack cardStack;

    public RewardCardUi(Skin skin){
        if (skin == null) throw new IllegalArgumentException("Skin cannot be null.");

        this.skin = skin;
        setFillParent(true);

        dimOverlay = new Image(skin.getDrawable("screen-dim"));
        dimOverlay.setColor(new Color(0f, 0f, 0f, 0f));

        cardImage = new Image();

        descriptionLabel = new Label("", skin);
        descriptionLabel.setAlignment(Align.center);
        descriptionLabel.setWrap(true);
        descriptionLabel.setFontScale(1f);

        Table descriptionLayer = new Table();
        descriptionLayer.setFillParent(true);
        descriptionLayer.bottom();
        descriptionLayer.add(descriptionLabel).width(DESCRIPTION_WIDTH).padBottom(DESCRIPTION_BOTTOM_PADDING);

        cardStack = new Stack();
        cardStack.add(cardImage);
        cardStack.add(descriptionLayer);

        Table cardLayer = new Table();
        cardLayer.setFillParent(true);
        cardLayer.center();
        cardLayer.add(cardStack).width(CARD_WIDTH).height(CARD_HEIGHT);

        Stack root = new Stack();
        root.add(dimOverlay);
        root.add(cardLayer);

        add(root).grow();
        setVisible(false);
    }

    public void showReward(RewardType rewardType, String description){
        if (rewardType == null) throw new IllegalArgumentException("Reward type cannot be null.");
        if (description == null) throw new IllegalArgumentException("Reward description cannot be null.");

        switch (rewardType){
            case SWIFT_STEP -> cardImage.setDrawable(skin.getDrawable("reward-swift-step"));
            case BLACKTHORN_CRAFT -> cardImage.setDrawable(skin.getDrawable("reward-blackthorn-craft"));
            case BLOODTHIRST -> cardImage.setDrawable(skin.getDrawable("reward-bloodthirst"));
        }

        descriptionLabel.setText(description);
        startAnimation();
    }

    private void startAnimation(){
        dimOverlay.clearActions();
        cardStack.clearActions();

        setVisible(true);
        validate();

        dimOverlay.getColor().a = 0f;

        cardStack.getColor().a = 0f;
        cardStack.setScale(0.82f);
        cardStack.setOrigin(Align.center);

        dimOverlay.addAction(
            Actions.sequence(
                Actions.alpha(DIM_ALPHA, 0.22f, Interpolation.fade),
                Actions.delay(DISPLAY_DURATION + 0.15f),
                Actions.alpha(0f, 0.30f, Interpolation.fade)
            )
        );

        cardStack.addAction(
            Actions.sequence(
                Actions.parallel(
                    Actions.alpha(1f, 0.16f, Interpolation.fade),
                    Actions.scaleTo(1.06f, 1.06f, 0.24f, Interpolation.swingOut)
                ),
                Actions.scaleTo(1f, 1f, 0.10f, Interpolation.smooth),
                Actions.delay(DISPLAY_DURATION),
                Actions.parallel(
                    Actions.alpha(0f, 0.28f, Interpolation.fade),
                    Actions.scaleTo(0.96f, 0.96f, 0.28f, Interpolation.fade)
                ),
                Actions.run(() -> setVisible(false))
            )
        );
    }

    public void hideReward(){
        dimOverlay.clearActions();
        cardStack.clearActions();

        setVisible(false);

        dimOverlay.getColor().a = 0f;
        cardStack.getColor().a = 1f;
        cardStack.setScale(1f);
    }
}
