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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

import static com.ashveil.Config.SCREEN_HEIGHT;
import static com.ashveil.Config.SCREEN_WIDTH;

public class HudRenderer {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera hudCamera;
    private Viewport hudViewport;
    private final Vector2 mousePosition;


    private CraftingCategory selectedCategory;

    public HudRenderer(){
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        selectedCategory = CraftingCategory.WEAPONS;
        hudCamera = new OrthographicCamera();
        hudViewport = new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, hudCamera);
        mousePosition = new Vector2();
    }

    public void render(Player player, DayNightCycle dayNightCycle, boolean menuOpen, List<Recipe> recipes){
        hudViewport.apply();
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        batch.setProjectionMatrix(hudCamera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawHearts(player);
        drawHotbar(player);
        drawDayNightIcon(dayNightCycle);
        drawMenuBackground(menuOpen);
        shapeRenderer.end();

        batch.begin();
        drawDayText(dayNightCycle);
        drawMenuContent(menuOpen, recipes);
        batch.end();
    }

    public void dispose(){
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }

    private void drawHearts(Player player){
        int hp = player.getCurrentHp();
        int maxHp = player.getMaxHp();

        for (int i=0; i<maxHp; i++){
            if (i < hp){
                shapeRenderer.setColor(1f, 0f, 0f, 1f);
            } else {
                shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
            }
            shapeRenderer.rect(10 + i * 25, hudViewport.getWorldHeight() - 30, 20, 20);
        }
    }

    private void drawHotbar(Player player){
        ItemStack[] slots = player.getInventory().getSlots();
        int slotSize = 40;
        int startX = (int) ((hudViewport.getWorldWidth() / 2f) - (Config.HOTBAR_SIZE * slotSize) / 2);

        for (int i = 0; i < Config.HOTBAR_SIZE; i++) {
            if (slots[i] != null) {
                shapeRenderer.setColor(0.9f, 0.5f, 0.1f, 1f);
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            }
            shapeRenderer.rect(startX + i * (slotSize + 5), 10, slotSize, slotSize);
        }
    }

    private void drawDayNightIcon(DayNightCycle dayNightCycle){
        if (!dayNightCycle.isNight()){
            shapeRenderer.setColor(1f, 0.9f, 0f, 1f);
            shapeRenderer.rect(hudViewport.getWorldWidth() - 80f, hudViewport.getWorldHeight() - 80f, 50, 50);
        }
        else{
            shapeRenderer.setColor(0.1f, 0.1f, 0.4f, 1f);
            shapeRenderer.rect(hudViewport.getWorldWidth() - 80f, hudViewport.getWorldHeight() - 80f, 50, 50);
        }
    }

    private void drawDayText(DayNightCycle dayNightCycle){
        font.draw(batch, "DAY: " + dayNightCycle.getDayCount(),
            hudViewport.getWorldWidth() - 100f,
            hudViewport.getWorldHeight() - 10f);
    }

    private void drawMenuBackground(boolean menuOpen) {
        if (menuOpen) {
            shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1f);
            shapeRenderer.rect(100, 100, hudViewport.getWorldWidth() - 200, hudViewport.getWorldWidth() - 200);
        }
    }

    private void drawMenuContent(boolean menuOpen, List<Recipe> recipes){
        if(!menuOpen) return;
        int tabX = 130;
        for (CraftingCategory cat : CraftingCategory.values()) {
            if (cat == selectedCategory) {
                font.setColor(1f, 1f, 0f, 1f); // žuta = selektovana
            } else {
                font.setColor(1f, 1f, 1f, 1f); // bela = ostale
            }
            font.draw(batch, cat.name(), tabX, hudViewport.getWorldHeight() - 110);
            tabX += 150;
        }
        float y = hudViewport.getWorldHeight() - 140;
        for (Recipe r : recipes) {
            if (r.getCategory() != selectedCategory) continue;
            font.draw(batch, r.getResultType().name(), 130, y);
            y -= 25;
        }
    }

    public void resize(int width, int height){
        hudViewport.update(width, height, true);
    }

    public CraftingCategory getCategoryAtScreenClick(int screenX, int screenY) {
        mousePosition.set(screenX, screenY);
        hudViewport.unproject(mousePosition);

        return getCategoryAtHudClick(mousePosition.x, mousePosition.y);
    }

    private CraftingCategory getCategoryAtHudClick(float hudX, float hudY) {
        int tabX = 130;

        for (CraftingCategory category : CraftingCategory.values()) {
            if (hudX >= tabX && hudX <= tabX + 140
                && hudY >= hudViewport.getWorldWidth() - 125
                && hudY <= hudViewport.getWorldHeight() - 105) {
                return category;
            }

            tabX += 150;
        }

        return null;
    }

    public void setSelectedCategory(CraftingCategory cat) {
        selectedCategory = cat;
    }

}
