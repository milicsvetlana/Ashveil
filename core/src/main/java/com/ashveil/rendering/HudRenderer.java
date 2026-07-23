package com.ashveil.rendering;

import com.ashveil.Config;
import com.ashveil.entities.Player;
import com.ashveil.items.crafting.CraftingCategory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.crafting.Recipe;
import com.ashveil.world.DayNightCycle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

import java.util.List;

import static com.ashveil.Config.SCREEN_HEIGHT;
import static com.ashveil.Config.SCREEN_WIDTH;

public class HudRenderer {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    private CraftingCategory selectedCategory;
    private int selectedRecipeIndex;
    Recipe selectedRecipe;

    public HudRenderer(){
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        selectedCategory = CraftingCategory.WEAPONS;
        selectedRecipeIndex = 0;
        selectedRecipe = null;
    }

    public void render(Player player, DayNightCycle dayNightCycle, boolean craftingOpen, List<Recipe> recipes){
        shapeRenderer.setProjectionMatrix(
            new Matrix4().setToOrtho2D(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT));

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int hp = player.getCurrentHp();
        int maxHp = player.getMaxHp();

        for (int i=0; i<maxHp; i++){
            if (i < hp){
                shapeRenderer.setColor(1f, 0f, 0f, 1f);
            } else {
                shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
            }
            shapeRenderer.rect(10 + i * 25, Config.SCREEN_HEIGHT - 30, 20, 20);
        }

        ItemStack[] slots = player.getInventory().getSlots();
        int slotSize = 40;
        int startX = Config.SCREEN_WIDTH / 2 - (slots.length * slotSize) / 2;

        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                shapeRenderer.setColor(0.9f, 0.5f, 0.1f, 1f);
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            }
            shapeRenderer.rect(startX + i * (slotSize + 5), 10, slotSize, slotSize);
        }

        if (!dayNightCycle.isNight()){
            shapeRenderer.setColor(1f, 0.9f, 0f, 1f);
            shapeRenderer.rect(Config.SCREEN_WIDTH - 80f, Config.SCREEN_HEIGHT - 80f, 50, 50);
        }
        else{
            shapeRenderer.setColor(0.1f, 0.1f, 0.4f, 1f);
            shapeRenderer.rect(Config.SCREEN_WIDTH - 80f, Config.SCREEN_HEIGHT - 80f, 50, 50);
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT));
        batch.begin();
        font.draw(batch, "DAY: " + dayNightCycle.getDayCount(),
            Config.SCREEN_WIDTH - 100,
            Config.SCREEN_HEIGHT - 10);
        batch.end();

        if (craftingOpen) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(100, 100, SCREEN_WIDTH - 200, SCREEN_HEIGHT - 200);
            shapeRenderer.end();

            batch.begin();
            int tabX = 130;
            for (CraftingCategory cat : CraftingCategory.values()) {
                if (cat == selectedCategory) {
                    font.setColor(1f, 1f, 0f, 1f); // žuta = selektovana
                } else {
                    font.setColor(1f, 1f, 1f, 1f); // bela = ostale
                }
                font.draw(batch, cat.name(), tabX, SCREEN_HEIGHT - 110);
                tabX += 150;
            }
            int y = SCREEN_HEIGHT - 140;
            for (Recipe r : recipes) {
                if (r.getCategory() != selectedCategory) continue;
                font.draw(batch, r.getResultType().name(), 130, y);
                y -= 25;
            }

            batch.end();
        }
    }

    public void dispose(){
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }

    public CraftingCategory getCategoryAtClick(float mouseX, float mouseY) {
        int tx = 130;
        for (CraftingCategory cat : CraftingCategory.values()) {
            if (mouseX >= tx && mouseX <= tx + 140 &&
                mouseY >= SCREEN_HEIGHT - 125 && mouseY <= SCREEN_HEIGHT - 105) {
                return cat;
            }
            tx += 150;
        }
        return null;
    }

    public void setSelectedCategory(CraftingCategory cat) {
        selectedCategory = cat;
        selectedRecipeIndex = 0;
    }

}
