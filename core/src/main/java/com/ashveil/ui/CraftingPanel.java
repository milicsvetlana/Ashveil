package com.ashveil.ui;

import com.ashveil.items.crafting.*;
import com.ashveil.items.inventory.ItemType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.List;
import java.util.Map;

//sadrzace kasnije listu recepata, kategorije kao obicne naslove, details panel, crafting dugme
public class CraftingPanel extends Table {

    private final Table recipeListTable;
    private final ScrollPane recipeScrollPane;
    private final Table detailsTable;
    private final List<Recipe> recipes;
    private final CraftingAccess craftingAccess;
    private Recipe selectedRecipe;

    public CraftingPanel(Skin skin, List<Recipe> recipes, CraftingAccess craftingAccess) {
        super(skin);
        this.recipes = recipes;
        this.craftingAccess = craftingAccess;

        recipeListTable = new Table();
        detailsTable = new Table();
        recipeScrollPane = new ScrollPane(recipeListTable);

        createLayout();
        createRecipeList();
        refreshDetails();
    }

    public void refresh(){
        refreshDetails();
    }

    private void createLayout(){
        add(recipeScrollPane).width(300).growY();
        add(detailsTable).grow().padLeft(20);
    }

    private void createRecipeList(){
        recipeListTable.clearChildren();

        for (CraftingCategory category : CraftingCategory.values()){
            Label categoryLabel = new Label(category.name(), getSkin());

            recipeListTable.add(categoryLabel).left().padTop(12).padBottom(5);
            recipeListTable.row();

            for (Recipe recipe : recipes){
                if (recipe.getCategory() == category){
                    TextButton recipeButton = new TextButton(recipe.getResultType().getDisplayName(), getSkin());
                    recipeListTable.add(recipeButton).growX().left().padBottom(4);
                    recipeListTable.row();
                    recipeButton.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent changeEvent, Actor actor) {
                            selectRecipe(recipe);
                        }
                    });
                }
            }
        }
    }

    private void selectRecipe(Recipe recipe){
        selectedRecipe = recipe;
        refreshDetails();
    }

    private void refreshDetails(){
        detailsTable.clearChildren();
        if (selectedRecipe == null) {
            detailsTable.add(new Label("Select a recipe", getSkin()));
            return;
        }

        detailsTable.add(new Label(selectedRecipe.getResultType().getDisplayName(), getSkin()));
        detailsTable.row();

        detailsTable.add(new Label("Required:", getSkin()));
        detailsTable.row();

        for (Map.Entry<ItemType, Integer> ingredient : selectedRecipe.getIngredients().entrySet()){
            ItemType itemType = ingredient.getKey();
            int requiredQuantity = ingredient.getValue();

            detailsTable.add(new Label(itemType.getDisplayName() + ": " + craftingAccess.getOwnedQuantity(itemType) + " / " + requiredQuantity, getSkin()));
            detailsTable.row();
        }

        detailsTable.add(new Label("Produces: " + selectedRecipe.getResultType().getDisplayName(), getSkin()));
        detailsTable.row();

        TextButton craftButton = new TextButton("CRAFT", getSkin());

        CraftStatus status = craftingAccess.getCraftStatus(selectedRecipe.getId());
        craftButton.setDisabled(status != CraftStatus.SUCCESS);

        detailsTable.add(craftButton);
        detailsTable.row();
        craftButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                craftSelectedRecipe();
            }
        });
    }

    private void craftSelectedRecipe(){
        CraftingResult result = craftingAccess.tryCraft(selectedRecipe.getId());
        refreshDetails();
    }
}
