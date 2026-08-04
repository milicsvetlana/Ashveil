package com.ashveil.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

//glavni kontejner

public class GameMenuUi {
    private final Stage stage;
    private final Table rootTable;
    private final Table contentTable;
    private final Skin skin;

    private final InventoryPanel inventoryPanel;
    private final CraftingPanel craftingPanel;
    private final ShopPanel shopPanel;

    private final TextButton inventoryButton;
    private final TextButton craftingButton;
    private final TextButton shopButton;

    private MenuTab selectedTab;

    public GameMenuUi() {
        skin = UiSkinFactory.create();
        stage = new Stage(new ScreenViewport());
        rootTable = new Table();
        rootTable.setFillParent(true); //znaci da tabela zauzima ceo stage
        contentTable = new Table();

        inventoryPanel = new InventoryPanel(skin);
        craftingPanel = new CraftingPanel(skin);
        shopPanel = new ShopPanel(skin);

        inventoryButton = new TextButton("Inventory", skin);
        craftingButton = new TextButton("Crafting", skin);
        shopButton = new TextButton("Shop", skin);

        createLayout();
        createListeners();
        showPanel(MenuTab.INVENTORY);
    }

    private void createLayout(){
        rootTable.add(inventoryButton).pad(8);
        rootTable.add(craftingButton).pad(8);
        rootTable.add(shopButton).pad(8);

        rootTable.row();
        rootTable.add(contentTable).colspan(3).grow().pad(12);
        stage.addActor(rootTable);
    }

    private void createListeners(){
        inventoryButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                showPanel(MenuTab.INVENTORY);
            }
        });

        craftingButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                showPanel(MenuTab.CRAFTING);
            }
        });

        shopButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                showPanel(MenuTab.SHOP);
            }
        });
    }

    private void showPanel(MenuTab menuTab){
        this.selectedTab = menuTab;
        contentTable.clearChildren();

        switch (menuTab){
            case INVENTORY -> contentTable.add(inventoryPanel).grow();
            case CRAFTING -> contentTable.add(craftingPanel).grow();
            case SHOP -> contentTable.add(shopPanel).grow();
        }
    }

    public void act(float delta){stage.act(delta);}
    public void draw(){stage.draw();}
    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }

    public Stage getStage() {return stage;}

    public void dispose(){
        stage.dispose();
        skin.dispose();
    }
}
