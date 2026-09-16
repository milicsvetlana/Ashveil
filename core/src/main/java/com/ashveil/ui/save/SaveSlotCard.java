package com.ashveil.ui.save;

import com.ashveil.save.SaveSlotInfo;
import com.ashveil.save.SaveSlotStatus;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;

public class SaveSlotCard extends Stack {
    private final Skin skin;
    private final SaveSlotInfo slotInfo;
    private final Image backgroundImage;
    private boolean hovered;

    private final Runnable primaryAction;
    private final Runnable deleteAction;

    public SaveSlotCard(Skin skin, SaveSlotInfo slotInfo, Runnable primaryAction, Runnable deleteAction){
        if (skin == null) throw new IllegalArgumentException("Skin cannot be null.");
        if (slotInfo == null) throw new IllegalArgumentException("SlotSaveInfo cannot be null.");

        this.skin = skin;
        this.slotInfo = slotInfo;
        this.backgroundImage = new Image(skin.getDrawable("save-slot-card-normal"));
        this.hovered = false;
        this.primaryAction = primaryAction;
        this.deleteAction = deleteAction;

        build();
        setTouchable(Touchable.enabled);
        addHoverListener();
    }

    private void build(){
        Table content = new Table();

        switch (slotInfo.getStatus()){
            case EMPTY -> buildEmptyContent(content);
            case VALID -> buildValidContent(content);
            case INVALID -> buildInvalidContent(content);
        }

        add(backgroundImage);
        add(content);
        refreshBackground();
    }

    private void refreshBackground(){
        String drawableName;

        if (slotInfo.getStatus() == SaveSlotStatus.INVALID){
            drawableName = "save-slot-card-unavailable";
        }
        else if (hovered){
            drawableName = "save-slot-card-hover";
        }
        else {
            drawableName = "save-slot-card-normal";
        }

        backgroundImage.setDrawable(skin.getDrawable(drawableName));
    }

    private void addHoverListener(){
        addListener(new InputListener(){
           @Override
           public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
               hovered = true;
               refreshBackground();
           }
           @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor){
               hovered = false;
               refreshBackground();
           }
        });
    }

    private void buildEmptyContent(Table content){
        Stack portraitStack = createStatusIcon();
        Label slotLabel = new Label("Slot " + slotInfo.getSlot(), skin);
        slotLabel.setFontScale(1.3f);

        Label emptyLabel = new Label("Empty", skin);
        TextButton newGameButton = new TextButton("New Game", skin, "save-slot-action");
        bindAction(newGameButton, primaryAction);

        Table info = new Table();
        info.top().left();
        info.add(slotLabel).left().padBottom(12f);
        info.row();
        info.add(emptyLabel).left();

        Table actions = new Table();
        actions.add(newGameButton).width(170f).height(43f).padTop(30f);

        content.pad(22f, 32f, 22f, 32f);
        content.add(portraitStack).size(140f, 140f).padLeft(30f).padRight(24f);
        content.add(info).expand().left().top().padTop(40f);
        content.add(actions).right().top().padTop(52f).padRight(30f);
    }

    private void buildValidContent(Table content){
        Stack portraitStack = createPortraitFrame();

        String displayName = slotInfo.getCharacterName();
        if (displayName == null || displayName.isBlank()) displayName = "Slot " + slotInfo.getSlot();
        Label slotLabel = new Label(displayName, skin);

        slotLabel.setFontScale(1.3f);
        Label dayLabel = new Label("Day " + slotInfo.getDayCount(), skin);
        Label playTimeLabel = new Label("Playtime: " + formatPlayTime(slotInfo.getPlayTimeSeconds()), skin);
        Label areaLabel = new Label("Area: " + slotInfo.getCurrentAreaId(), skin);

        TextButton playButton = new TextButton("Play", skin, "save-slot-action");
        TextButton deleteButton = new TextButton("Delete", skin, "save-slot-action");
        bindAction(playButton, primaryAction);
        bindAction(deleteButton, deleteAction);

        Table info = new Table();
        info.top().left();

        info.add(slotLabel).left().padBottom(12f);
        info.row();

        info.add(dayLabel).left().padBottom(6f);
        info.row();

        info.add(playTimeLabel).left().padBottom(6f);
        info.row();

        info.add(areaLabel).left();

        Table actions = new Table();
        actions.add(playButton).width(170f).height(43f);
        actions.row();
        actions.add(deleteButton).width(170f).height(43f).padTop(8f);

        content.pad(22f, 32f, 22f, 32f);
        content.add(portraitStack).size(140f, 140f).padRight(24f).padLeft(30f);
        content.add(info).expand().left().top().padTop(45f);
        content.add(actions).right().top().padTop(52f).padRight(30f);
    }

    private String formatPlayTime(double playTimeSeconds){
        long totalSeconds = (long) playTimeSeconds;

        long hours = totalSeconds / 3600;
        long mintues = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d.%02d.%02d", hours, mintues, seconds);
    }

    private void buildInvalidContent(Table content){
        Stack portraitStack = createStatusIcon();

        Label slotLabel = new Label("Slot " + slotInfo.getSlot(), skin);
        slotLabel.setFontScale(1.3f);
        Label unavailableLabel = new Label("Unavailable", skin);
        Label messageLabel = new Label("Save data could not be loaded.", skin);

        TextButton deleteButton = new TextButton("Delete", skin, "save-slot-action");
        bindAction(deleteButton, deleteAction);

        Table info = new Table();
        info.top().left();
        info.add(slotLabel).left().padBottom(12f);
        info.row();
        info.add(unavailableLabel).left().padBottom(6f);
        info.row();
        info.add(messageLabel).left();

        Table actions = new Table();
        actions.add(deleteButton).width(170f).height(43f);

        content.pad(22f, 32f, 22f, 32f);
        content.add(portraitStack).size(140f, 140f).padLeft(30f).padRight(24f);
        content.add(info).expand().left().top().padTop(40f);
        content.add(actions).right().top().padTop(52f).padRight(30f);
    }

    private Stack createStatusIcon(){
        Stack stack = new Stack();
        Image icon = new Image(skin.getDrawable("save-slot-empty-icon"));
        icon.setScaling(Scaling.fit);

        Table iconLayer = new Table();
        iconLayer.pad(38f);
        iconLayer.add(icon).size(104f, 104f);

        Image frame = new Image(skin.getDrawable("save-slot-portrait-frame"));

        stack.add(iconLayer);
        stack.add(frame);
        return stack;
    }

    private Stack createPortraitFrame() {
        Stack stack = new Stack();
        Image frame = new Image(skin.getDrawable("save-slot-portrait-frame"));
        stack.add(frame);
        return stack;
    }

    private void bindAction(TextButton button, Runnable action){
        button.setDisabled(action == null);

        if (action == null) return;

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                action.run();
            }
        });
    }

}
