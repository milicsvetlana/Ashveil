package com.ashveil.items;

import java.util.*;

public class CraftingManager {
    List<Recipe> recipes;
    private Set<CraftingCategory> unlockedCategories = new HashSet<>();

    public CraftingManager() {
        unlockedCategories.add(CraftingCategory.WEAPONS);
        unlockedCategories.add(CraftingCategory.TOOLS);
        unlockedCategories.add(CraftingCategory.FOOD);
        unlockedCategories.add(CraftingCategory.BUILDING);
        recipes = RecipeBook.getAllRecipes();
    }

    private boolean canCraft(Recipe recipe, Inventory inventory){
        for (Map.Entry<ItemType, Integer> ingredient : recipe.getIngredients().entrySet()){
            ItemType type = ingredient.getKey();
            int required = ingredient.getValue();
            if (inventory.getQuantity(type) < required) return false;
        }
        return true;
    }

    public void craft(Recipe recipe, Inventory inventory){
        if (!canCraft(recipe, inventory)) return;
        for (Map.Entry<ItemType, Integer> ingredient : recipe.getIngredients().entrySet()) {
            ItemType type = ingredient.getKey();
            int required = ingredient.getValue();
            inventory.removeItem(type, required);
        }
        inventory.addItem(recipe.getResultType(), recipe.getResultAmount());
    }

    public boolean isCategoryUnlocked(CraftingCategory category) {return unlockedCategories.contains(category);}
    public void unlockCategory(CraftingCategory category) {unlockedCategories.add(category);}
    public List<Recipe> getRecipes() {return recipes;}
}
