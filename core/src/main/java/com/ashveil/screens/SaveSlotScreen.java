package com.ashveil.screens;

import com.ashveil.GameApp;
import com.ashveil.save.SaveSlotInfo;
import com.ashveil.save.SaveSlotStatus;
import com.ashveil.ui.save.SaveSlotCard;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SaveSlotScreen implements Screen {
    private final GameApp game;
    private final Stage stage;
    private final Skin skin;
    private final Texture backgroundTexture;
    private Stack activeModal;

    public SaveSlotScreen(GameApp game){
        if (game == null) throw new IllegalArgumentException("Game cannot be null.");
        this.game = game;

        stage = new Stage(new ScreenViewport());
        skin = game.getUiSkin();
        backgroundTexture = new Texture("ui/save-slots/save-slots-background.png");
        activeModal = null;

        buildBackground();
        buildUi();
    }

    private void buildBackground(){
        Image background = new Image(backgroundTexture);
        background.setFillParent(true);
        background.setScaling(Scaling.fill);
        stage.addActor(background);

        Image dimOverlay = new Image(skin.getDrawable("screen-dim"));
        dimOverlay.setFillParent(true);
        dimOverlay.setColor(0f, 0f, 0f, 0.42f);
        stage.addActor(dimOverlay);
    }

    private void buildUi(){
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("SELECT SAVE", skin);
        Image titleDivider = new Image(skin.getDrawable("save-slots-title-divider"));
        titleDivider.setScaling(Scaling.fill);
        Table titleBlock = new Table();

        titleBlock.add(title);
        titleBlock.row();
        titleBlock.add(titleDivider).width(260f).height(18f).padTop(10f);

        TextButton backButton = new TextButton("Back", skin, "save-slot-action");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                game.showMainMenu();
            }
        });

        Table slotsTable = new Table();

        for (int slot = 1; slot <= 3; slot++){
            SaveSlotInfo slotInfo = game.getSaveService().getSlotInfo(slot);

            final int slotNumber = slot;
            Runnable deleteAction = slotInfo.getStatus() == SaveSlotStatus.EMPTY ? null : () -> showDeleteConfirmation(slotNumber);
            SaveSlotCard slotCard = new SaveSlotCard(skin, slotInfo, null, deleteAction);

            slotsTable.add(slotCard).width(720f).height(247f).padBottom(slot < 3 ? 8f : 0f);
            if (slot < 3) slotsTable.row();
        }

        root.add(titleBlock).padBottom(30f);
        root.row();
        root.add(slotsTable);
        root.row();
        root.add(backButton).width(180f).height(50f).padTop(16f);

        stage.addActor(root);
    }

    private void showDeleteConfirmation(int slotNumber){
        if (activeModal != null) return;

        activeModal = new Stack();
        activeModal.setFillParent(true);

        Image dim = new Image(skin.getDrawable("screen-dim"));
        dim.setColor(0f, 0f, 0f, 0.70f);

        Table dialogContainer = new Table();

        Table dialog = new Table();
        dialog.setBackground(skin.getDrawable("dialog-box"));
        dialog.pad(24f);

        Label message = new Label("Delete Slot " + slotNumber + "? This action cannot be undone.", skin);
        TextButton cancelButton = new TextButton("Cancel", skin, "save-slot-action");
        TextButton deleteButton = new TextButton("Delete", skin, "save-slot-action");

        dialog.add(message).colspan(2).padBottom(80f);
        dialog.row();
        dialog.add(cancelButton).width(150f).height(43f).padRight(8f);
        dialog.add(deleteButton).width(150f).height(43f).padLeft(8f);
        dialogContainer.add(dialog).width(600).height(390f);

        activeModal.add(dim);
        activeModal.add(dialogContainer);

        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                closeModal();
            }
        });

        deleteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                boolean deleted = game.getSaveService().deleteSlot(slotNumber);
                if (!deleted){
                    message.setText("Could not delete Slot " + slotNumber + ".");
                    return;
                }
                game.showSaveSlots();
            }
        });

        stage.addActor(activeModal);
    }

    private void closeModal(){
        if (activeModal == null) return;
        activeModal.remove();
        activeModal = null;
    }

    @Override
    public void render(float delta){
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            if (activeModal != null){
                closeModal();
                return;
            }

            game.showMainMenu();
            return;
        }

        ScreenUtils.clear(0.05f, 0.05f, 0.07f, 1f);

        stage.act(delta);
        stage.draw();
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void hide() {
        if (Gdx.input.getInputProcessor() == stage){
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }
}
