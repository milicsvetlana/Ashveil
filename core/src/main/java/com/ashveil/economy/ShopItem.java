package com.ashveil.economy;

import com.ashveil.items.inventory.ItemType;

public enum ShopItem {

    HEART_MEND(
        ItemType.HEART_REPAIR,
        20,
        "shop.heartMend",
        "shop.heartMendDescription"
    );

    private final ItemType itemType;
    private final int price;
    private final String nameKey;
    private final String descriptionKey;

    ShopItem(ItemType itemType, int price, String nameKey, String descriptionKey) {
        this.itemType = itemType;
        this.price = price;
        this.nameKey = nameKey;
        this.descriptionKey = descriptionKey;
    }

    public ItemType getItemType() {return itemType;}
    public int getPrice() {return price;}
    public String getNameKey() {return nameKey;}
    public String getDescriptionKey() {return descriptionKey;}
}
