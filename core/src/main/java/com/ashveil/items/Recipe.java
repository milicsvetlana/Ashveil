package com.ashveil.items;

import java.util.Map;

public class Recipe {
    private Map<ItemType, Integer> ingredients;
    private ItemType resultType;
    private int resultAmount;
    private CraftingCategory category;

    public Recipe(boolean locked, Map<ItemType, Integer> ingredients, ItemType resultType, int resultAmount, CraftingCategory category) {
        this.ingredients = ingredients;
        this.resultType = resultType;
        this.resultAmount = resultAmount;
        this.category = category;
    }

    public Map<ItemType, Integer> getIngredients() {return ingredients;}
    public ItemType getResultType() {return resultType;}
    public int getResultAmount() {return resultAmount;}
}
