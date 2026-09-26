package com.ashveil.economy;

public interface ShopAccess {

    int getGold();
    boolean canAfford(ShopItem shopItem);
    boolean buyShopItem(ShopItem shopItem);
}
