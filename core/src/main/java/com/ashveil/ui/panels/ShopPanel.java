package com.ashveil.ui.panels;

import com.ashveil.economy.ShopAccess;
import com.ashveil.economy.ShopItem;
import com.ashveil.localization.LocalizationService;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class ShopPanel extends MenuPanel {

    private final ShopAccess shopAccess;
    private final LocalizationService i18n;

    private final Table itemListTable;
    private final Table detailsTable;

    private ShopItem selectedItem;
    private TextButton selectedItemButton;

    public ShopPanel(Skin skin, ShopAccess shopAccess, LocalizationService i18n) {
        super(skin);

        this.shopAccess = shopAccess;
        this.i18n = i18n;

        itemListTable = new Table();
        detailsTable = new Table();

        createLayout();
        createItemList();

        selectFirstItem();
    }

    private void createLayout(){
        itemListTable.top().left();
        detailsTable.top().left();
        add(itemListTable).width(300).growY();
        add(detailsTable).grow().padLeft(30);
    }

    private void createItemList(){
        for (ShopItem shopItem : ShopItem.values()){
            TextButton itemButton = new TextButton(i18n.get(shopItem.getNameKey()), getSkin());

            itemButton.setProgrammaticChangeEvents(false);

            itemListTable.add(itemButton).growX().left().padBottom(6);
            itemListTable.row();

            itemButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    selectItem(shopItem, itemButton);
                }
            });
        }
    }

    private void selectFirstItem(){
        if (ShopItem.values().length == 0) return;

        selectedItem = ShopItem.values()[0];
        refreshDetails();
    }

    private void selectItem(ShopItem shopItem, TextButton itemButton) {
        if (selectedItemButton != null && selectedItemButton != itemButton) selectedItemButton.setChecked(false);

        selectedItem = shopItem;
        selectedItemButton = itemButton;

        selectedItemButton.setChecked(true);

        refreshDetails();
    }

    private void refreshDetails(){
        detailsTable.clearChildren();

        detailsTable.add(new Label(i18n.get("shop.gold") + ": " + shopAccess.getGold(), getSkin())).left();
        detailsTable.row();

        if (selectedItem == null) {
            detailsTable.add(new Label(i18n.get("shop.selectItem"), getSkin())).padTop(20).left();
            return;
        }

        String name = i18n.get(selectedItem.getNameKey());
        String description = i18n.get(selectedItem.getDescriptionKey());

        detailsTable.add(new Label(name, getSkin())).padTop(25).left();
        detailsTable.row();

        Label descriptionLabel = new Label(description, getSkin());
        descriptionLabel.setWrap(true);

        detailsTable.add(descriptionLabel).width(350).padTop(20).left().top();
        detailsTable.row();
        detailsTable.add(new Label(i18n.get("shop.price") + ": " +
                                        selectedItem.getPrice() + " " + i18n.get("shop.gold"), getSkin())).padTop(20).left();
        detailsTable.row();

        TextButton buyButton = new TextButton(i18n.get("shop.buy"), getSkin());
        buyButton.setDisabled(!shopAccess.canAfford(selectedItem));

        detailsTable.add(buyButton).width(160).padTop(20).left();

        buyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buySelectedItem();
            }
        });
    }

    private void buySelectedItem(){
        if (selectedItem == null) return;
        if (!shopAccess.canAfford(selectedItem)) return;
        if (shopAccess.buyShopItem(selectedItem)) refreshDetails();
    }

    @Override
    public void refresh(){refreshDetails();}

    @Override
    public void confirmSelection(){buySelectedItem();}
}
