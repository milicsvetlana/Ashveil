package com.ashveil.ui;

import com.ashveil.items.crafting.CraftingAccess;
import com.ashveil.items.crafting.Recipe;
import com.ashveil.items.inventory.Inventory;
import com.ashveil.ui.panels.CraftingPanel;
import com.ashveil.ui.panels.InventoryPanel;
import com.ashveil.ui.panels.MenuPanel;
import com.ashveil.ui.panels.ShopPanel;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.List;

//glavni kontejner

public class GameMenuUi {
    private final Stage stage;
    private final Table rootTable;
    private final Table contentTable;
    private final Table menuTable;
    private final Skin skin;

    private final InventoryPanel inventoryPanel;
    private final CraftingPanel craftingPanel;
    private final ShopPanel shopPanel;

    private final TextButton inventoryButton;
    private final TextButton craftingButton;
    private final TextButton shopButton;

    private MenuTab selectedTab;

    public GameMenuUi(Skin skin, List<Recipe> recipes, CraftingAccess craftingAccess, Inventory inventory) {
        this.skin = skin;
        stage = new Stage(new ScreenViewport());
        rootTable = new Table();
        rootTable.setFillParent(true); //znaci da tabela zauzima ceo stage
        contentTable = new Table();
        menuTable = new Table();
        menuTable.setBackground(skin.getDrawable("menu-background"));

        inventoryPanel = new InventoryPanel(skin, inventory);
        craftingPanel = new CraftingPanel(skin, recipes, craftingAccess);
        shopPanel = new ShopPanel(skin);

        inventoryButton = new TextButton("Inventory", skin);
        craftingButton = new TextButton("Crafting", skin);
        shopButton = new TextButton("Shop", skin);
        inventoryButton.setProgrammaticChangeEvents(false);
        craftingButton.setProgrammaticChangeEvents(false);
        shopButton.setProgrammaticChangeEvents(false);

        createLayout();
        createListeners();
        showPanel(MenuTab.INVENTORY);
    }

    public void onOpen(){
        switch (selectedTab) {
            case INVENTORY -> inventoryPanel.onShow();
            case CRAFTING -> craftingPanel.onShow();
            case SHOP -> shopPanel.onShow();
        }
    }

    private void createLayout(){
        menuTable.add(inventoryButton).pad(8);
        menuTable.add(craftingButton).pad(8);
        menuTable.add(shopButton).pad(8);

        menuTable.row();
        menuTable.add(contentTable).colspan(3).grow().pad(12);

        rootTable.add(menuTable).width(1000).height(650);
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

    private void showPanel(MenuTab menuTab) {
        if (selectedTab != null && selectedTab != menuTab) {
            getPanel(selectedTab).onHide();
        }

        selectedTab = menuTab;
        contentTable.clearChildren();

        MenuPanel panel = getPanel(menuTab);
        panel.onShow();

        updateTabButtons();
        contentTable.add(panel).grow();
    }

    private void updateTabButtons(){
        inventoryButton.setChecked(selectedTab == MenuTab.INVENTORY);
        craftingButton.setChecked(selectedTab == MenuTab.CRAFTING);
        shopButton.setChecked(selectedTab == MenuTab.SHOP);
    }

    private MenuPanel getPanel(MenuTab menuTab) {
        return switch (menuTab) {
            case INVENTORY -> inventoryPanel;
            case CRAFTING -> craftingPanel;
            case SHOP -> shopPanel;
        };
    }

    public void onClose() {
        if (selectedTab != null) {
            getPanel(selectedTab).onHide();
        }
    }

    public void showPreviousTab(){
        switch (selectedTab){
            case SHOP -> showPanel(MenuTab.CRAFTING);
            case INVENTORY -> showPanel(MenuTab.SHOP);
            case CRAFTING -> showPanel(MenuTab.INVENTORY);
        }
    }

    public void showNextTab() {
        switch (selectedTab) {
            case INVENTORY -> showPanel(MenuTab.CRAFTING);
            case CRAFTING -> showPanel(MenuTab.SHOP);
            case SHOP -> showPanel(MenuTab.INVENTORY);
        }
    }

    public void moveSelectionUp(){getPanel(selectedTab).moveSelectionUp();}
    public void moveSelectionDown(){getPanel(selectedTab).moveSelectionDown();}
    public void moveSelectionLeft(){getPanel(selectedTab).moveSelectionLeft();}
    public void moveSelectionRight(){getPanel(selectedTab).moveSelectionRight();}
    public void confirmSelection() {getPanel(selectedTab).confirmSelection();}

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
