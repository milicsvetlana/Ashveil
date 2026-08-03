package com.ashveil.items.crafting;

import com.ashveil.items.inventory.ItemType;

import java.util.Map;

public class Recipe {
    private final String id;
    private final Map<ItemType, Integer> ingredients;
    private final ItemType resultType;
    private final int resultAmount;
    private final CraftingCategory category;

    public Recipe(String id, Map<ItemType, Integer> ingredients, ItemType resultType, int resultAmount, CraftingCategory category) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Recipe ID cannot be null or blank");
        if (ingredients == null || ingredients.isEmpty()) throw new IllegalArgumentException(("Recipe must contain at least one ingredient."));
        for (Map.Entry<ItemType, Integer> ingredient : ingredients.entrySet()){
            if (ingredient.getKey() == null) throw new IllegalArgumentException("Ingredient type cannot be null");
            if (ingredient.getValue() == null || ingredient.getValue() <= 0) throw new IllegalArgumentException("Ingredient amount must be positive for: " + ingredient.getKey());
        }
        if (resultType == null) throw new IllegalArgumentException("Recipe result type cannot be null.");
        if (resultAmount <= 0) throw new IllegalArgumentException("Recipe result amount must be positive.");
        if (category == null) throw new IllegalArgumentException("Recipe category cannot be null.");
        this.id = id.trim();
        this.ingredients = Map.copyOf(ingredients);
        this.resultType = resultType;
        this.resultAmount = resultAmount;
        this.category = category;
    }

    public String getId() {return id;}
    public Map<ItemType, Integer> getIngredients() {return ingredients;}
    public ItemType getResultType() {return resultType;}
    public int getResultAmount() {return resultAmount;}
    public CraftingCategory getCategory() {return category;}
}
