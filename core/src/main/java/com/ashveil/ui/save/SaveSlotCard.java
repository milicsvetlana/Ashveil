package com.ashveil.ui.save;

import com.ashveil.save.SaveSlotInfo;
import com.ashveil.save.SaveSlotStatus;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;

public class SaveSlotCard extends Stack {
    private final Skin skin;
    private final SaveSlotInfo slotInfo;

    public SaveSlotCard(Skin skin, SaveSlotInfo slotInfo){
        if (skin == null) throw new IllegalArgumentException("Skin cannot be null.");
        if (slotInfo == null) throw new IllegalArgumentException("SlotSaveInfo cannot be null.");

        this.skin = skin;
        this.slotInfo = slotInfo;

        build();
    }

    private void build(){
        Stack card = new Stack();

        String backgroundStyle = slotInfo.getStatus() == SaveSlotStatus.INVALID ? "save-slot-card-unavailable" : "save-slot-card";
        Image cardBackground = new Image(skin.getDrawable(backgroundStyle));
        Table content = new Table();

        switch (slotInfo.getStatus()){
            case EMPTY -> buildEmptyContent(content);
            case VALID -> buildValidContent(content);
            case INVALID -> buildInvalidContent(content);
        }

        add(cardBackground);
        add(content);
    }


    private void buildEmptyContent(Table content){
        Stack portraitStack = new Stack();
        Image portraitFrame = new Image(skin.getDrawable("save-slot-portrait-frame"));
        portraitStack.add(portraitFrame);
        Label slotLabel = new Label("Slot " + slotInfo.getSlot(), skin);
        slotLabel.setFontScale(1.3f);

        Label emptyLabel = new Label("Empty", skin);
        TextButton newGameButton = new TextButton("New Game", skin, "save-slot-action");
        newGameButton.setDisabled(true);

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
        Stack portraitStack = new Stack();
        Image portraitImage = new Image(skin.getDrawable("save-slot-portrait-placeholder"));
        portraitImage.setScaling(Scaling.fill);
        Image portraitFrame = new Image(skin.getDrawable("save-slot-portrait-frame"));

        Table portraitLayer = new Table();
        portraitLayer.pad(29f, 25f, 29f, 25f);
        portraitLayer.add(portraitImage).expand().fill();

        portraitStack.add(portraitLayer);
        portraitStack.add(portraitFrame);

        Label slotLabel = new Label("Slot " + slotInfo.getSlot(), skin);
        slotLabel.setFontScale(1.3f);
        Label dayLabel = new Label("Day " + slotInfo.getDayCount(), skin);
        Label playTimeLabel = new Label("Playtime: " + formatPlayTime(slotInfo.getPlayTimeSeconds()), skin);
        Label areaLabel = new Label("Area: " + slotInfo.getCurrentAreaId(), skin);

        TextButton playButton = new TextButton("Play", skin, "save-slot-action");
        TextButton deleteButton = new TextButton("Delete", skin, "save-slot-action");
        playButton.setDisabled(true);
        deleteButton.setDisabled(true);

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
        Stack portraitStack = new Stack();
        Image portraitFrame = new Image(skin.getDrawable("save-slot-portrait-frame"));
        portraitStack.add(portraitFrame);

        Label slotLabel = new Label("Slot " + slotInfo.getSlot(), skin);
        slotLabel.setFontScale(1.3f);
        Label unavailableLabel = new Label("Unavailable", skin);
        Label messageLabel = new Label("Save data could not be loaded.", skin);

        TextButton deleteButton = new TextButton("Delete", skin, "save-slot-action");
        deleteButton.setDisabled(true);

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

}
