package com.ashveil.items.crafting;

import com.ashveil.items.inventory.ItemType;

import java.util.Map;

public class Recipe {
    private final Map<ItemType, Integer> ingredients;
    private final ItemType resultType;
    private final int resultAmount;
    private final CraftingCategory category;

    public Recipe(boolean locked, Map<ItemType, Integer> ingredients, ItemType resultType, int resultAmount, CraftingCategory category) {
        this.ingredients = ingredients;
        this.resultType = resultType;
        this.resultAmount = resultAmount;
        this.category = category;
    }

    public Map<ItemType, Integer> getIngredients() {return ingredients;}
    public ItemType getResultType() {return resultType;}
    public int getResultAmount() {return resultAmount;}
    public CraftingCategory getCategory() {return category;}
}
