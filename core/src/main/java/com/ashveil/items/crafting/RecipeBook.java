package com.ashveil.items.crafting;

import com.ashveil.items.inventory.ItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeBook {
    public static List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(new Recipe(false, Map.of(ItemType.WOOD, 2, ItemType.STONE, 1), ItemType.WOODEN_SWORD, 1, CraftingCategory.WEAPONS));
        // ...
        return recipes;
    }
}
