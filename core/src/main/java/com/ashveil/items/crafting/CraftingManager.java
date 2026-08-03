package com.ashveil.items.crafting;

import com.ashveil.items.inventory.Inventory;
import com.ashveil.items.inventory.ItemType;

import java.util.*;

public class CraftingManager {
    private final List<Recipe> recipes;
    private final Set<CraftingCategory> unlockedCategories = new HashSet<>();

    public CraftingManager() {
        unlockedCategories.add(CraftingCategory.WEAPONS);
        unlockedCategories.add(CraftingCategory.TOOLS);
        unlockedCategories.add(CraftingCategory.FOOD);
        unlockedCategories.add(CraftingCategory.BUILDING);
        recipes = RecipeBook.getAllRecipes();
    }

    public CraftStatus getCraftStatus(Recipe recipe, Inventory inventory){
        if (!isCategoryUnlocked(recipe.getCategory())) return CraftStatus.CATEGORY_LOCKED;
        if (!canCraft(recipe, inventory)) return CraftStatus.MISSING_INGREDIENTS;
        return CraftStatus.SUCCESS;
    }

    private boolean canCraft(Recipe recipe, Inventory inventory){
        for (Map.Entry<ItemType, Integer> ingredient : recipe.getIngredients().entrySet()){
            ItemType type = ingredient.getKey();
            int required = ingredient.getValue();
            if (inventory.getQuantity(type) < required) return false;
        }
        return true;
    }

    public CraftingResult craft(Recipe recipe, Inventory inventory){
        CraftStatus status = getCraftStatus(recipe, inventory);
        if (status != CraftStatus.SUCCESS){
            return new CraftingResult(status, recipe.getResultType(), 0, 0);
        }

        for (Map.Entry<ItemType, Integer> ingredient : recipe.getIngredients().entrySet()) {
            ItemType type = ingredient.getKey();
            int required = ingredient.getValue();
            inventory.removeItem(type, required);
        }

        int resultAmount = recipe.getResultAmount();
        int overflow = inventory.addItem(recipe.getResultType(), resultAmount);
        return new CraftingResult(status, recipe.getResultType(), resultAmount, overflow);

    }

    public Recipe getRecipeById(String recipeId){
        for (Recipe recipe : recipes){
            if (recipe.getId().equals(recipeId)) return recipe;
        }
        return null;
    }

    public boolean isCategoryUnlocked(CraftingCategory category) {return unlockedCategories.contains(category);}
    public void unlockCategory(CraftingCategory category) {unlockedCategories.add(category);}
    public List<Recipe> getRecipes() {return recipes;}
}
