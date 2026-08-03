package com.ashveil.items.crafting;

import com.ashveil.items.inventory.ItemType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.*;

public final class RecipeBook {
    private static final String RECIPES_PATH = "data/recipes.csv";
    private static final String EXPECTED_HEADER = "id,category,result_type,result_amount,ingredients";
    private static final int EXPECTED_COLUMN_COUNT = 5;

    private RecipeBook(){
    }

    public static List<Recipe> getAllRecipes(){
        FileHandle file = Gdx.files.internal(RECIPES_PATH);
        if (!file.exists()) throw new IllegalStateException("Recipe file was not found: " + RECIPES_PATH);

        String content = file.readString("UTF-8");
        String[] lines = content.split("\\R");

        if (lines.length == 0 || !lines[0].trim().equals(EXPECTED_HEADER)) throw new IllegalArgumentException("Invalid recipe CSV header. Expected: " + EXPECTED_HEADER);

        List<Recipe> recipes = new ArrayList<>();
        Set<String> recipeIds = new HashSet<>();

        for (int i=1; i < lines.length; i++){
            String line = lines[i].trim();

            if (line.isEmpty() || line.startsWith("#")) continue;

            Recipe recipe = parseRecipe(line, i+1);

            if (!recipeIds.add(recipe.getId())){
                throw new IllegalArgumentException("Duplicate recipe ID at line: " + i + 1 + ": " + recipe.getId());
            }

            recipes.add(recipe);
        }

        if (recipes.isEmpty()) throw new IllegalArgumentException("Recipe CSV does not contain any recipes.");

        return List.copyOf(recipes);
    }

    private static Recipe parseRecipe(String line, int lineNumber){
        String[] columns = line.split(",", -1);

        if (columns.length != EXPECTED_COLUMN_COUNT) throw new IllegalArgumentException(
            "Invalid number of columns at line " + lineNumber + ". Expected " + EXPECTED_COLUMN_COUNT +
                ", but found " + columns.length + ".");

        String id = columns[0].trim();
        CraftingCategory category = parseCategory(columns[1], lineNumber);
        ItemType resultType = parseItemType(columns[2], lineNumber);
        int resultAmount = parsePositiveInteger(columns[3], lineNumber, "result amount");
        Map<ItemType, Integer> ingredients = parseIngredients(columns[4], lineNumber);

        try{
            return new Recipe(id, ingredients, resultType, resultAmount, category);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid recipe at line: " + lineNumber + ": " + exception.getMessage(), exception);
        }
    }

    private static Map<ItemType, Integer> parseIngredients(String value, int lineNumber){
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Ingredients cannot be empty at line " + lineNumber + ".");

        Map<ItemType, Integer> ingredients = new LinkedHashMap<>();
        String[] ingredientParts = value.split("\\|");

        for (String ingredientPart : ingredientParts){
            String[] ingredientData = ingredientPart.trim().split(":", -1);

            if (ingredientData.length != 2){
                throw new IllegalArgumentException("Invalid ingredient format at line " + lineNumber + ": " + ingredientPart);
            }

            ItemType itemType = parseItemType(ingredientData[0], lineNumber);
            int amount = parsePositiveInteger(ingredientData[1], lineNumber, "ingredient amount");

            if (ingredients.put(itemType, amount) != null){
                throw new IllegalArgumentException("Duplicate ingredient at line " + lineNumber + ": " + itemType);
            }
        }

        return ingredients;
    }

    private static CraftingCategory parseCategory(String value, int lineNumber){
        try{
            return CraftingCategory.valueOf(value.trim());
        } catch (IllegalArgumentException exception){
            throw new IllegalArgumentException("Unknown crafting category at line "+ lineNumber + ": " + value, exception);
        }
    }

    private static ItemType parseItemType(String value, int lineNumber){
        try {
            return ItemType.valueOf(value.trim());
        } catch (IllegalArgumentException exception){
            throw new IllegalArgumentException("Unknown item type at line: " + lineNumber + ": " + value + exception);
        }
    }

    private static int parsePositiveInteger(String value, int lineNumber, String fieldName){
        try{
            int amount = Integer.parseInt(value.trim());
            if (amount <= 0) throw new IllegalArgumentException(fieldName + " must be positive at line " + lineNumber + ".");
            return amount;
        } catch (NumberFormatException exception){
            throw new IllegalArgumentException("Invalid " + fieldName + " at line " + lineNumber + ": " + value, exception);
        }
    }





}
