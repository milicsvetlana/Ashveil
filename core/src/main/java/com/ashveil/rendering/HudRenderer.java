package com.ashveil.rendering;

import com.ashveil.Config;
import com.ashveil.entities.Player;
import com.ashveil.items.crafting.CraftingCategory;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.crafting.Recipe;
import com.ashveil.items.inventory.ItemType;
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
        drawHotbarText(player);
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

    private void drawHotbar(Player player) {
        int slotSize = 40;
        int slotGap = 5;
        int slotY = 10;

        int hotbarWidth = Config.HOTBAR_SIZE * slotSize
            + (Config.HOTBAR_SIZE - 1) * slotGap;

        float startX = (hudViewport.getWorldWidth() - hotbarWidth) / 2f;

        for (int i = 0; i < Config.HOTBAR_SIZE; i++) {
            float slotX = startX + i * (slotSize + slotGap);

            if (i == player.getSelectedHotbarSlot()) {
                shapeRenderer.setColor(1f, 1f, 1f, 1f);
                shapeRenderer.rect(slotX - 3, slotY - 3, slotSize + 6, slotSize + 6);
            }

            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(slotX, slotY, slotSize, slotSize);

            ItemStack item = player.getInventory().getSlot(i);
            if (item == null) continue;

            setTemporaryItemColor(item.getType());
            shapeRenderer.rect(slotX + 7, slotY + 7, slotSize - 14, slotSize - 14);
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
            shapeRenderer.rect(100, 100, hudViewport.getWorldWidth() - 200, hudViewport.getWorldHeight() - 200);
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
                && hudY >= hudViewport.getWorldHeight() - 125
                && hudY <= hudViewport.getWorldHeight() - 105) {
                return category;
            }

            tabX += 150;
        }

        return null;
    }

    private void setTemporaryItemColor(ItemType type) {
        switch (type) {
            case WOOD -> shapeRenderer.setColor(0.45f, 0.25f, 0.1f, 1f);
            case STONE -> shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
            case WHEAT, BREAD -> shapeRenderer.setColor(0.9f, 0.75f, 0.2f, 1f);
            case WHEAT_SEED -> shapeRenderer.setColor(0.2f, 0.65f, 0.2f, 1f);
            case AXE, PICKAXE, HOE, SWORD -> shapeRenderer.setColor(0.65f, 0.25f, 0.15f, 1f);
            default -> shapeRenderer.setColor(0.6f, 0.4f, 0.7f, 1f);
        }
    }

    private void drawHotbarText(Player player) {
        int slotSize = 40;
        int slotGap = 5;
        int slotY = 10;

        int hotbarWidth = Config.HOTBAR_SIZE * slotSize
            + (Config.HOTBAR_SIZE - 1) * slotGap;

        float startX = (hudViewport.getWorldWidth() - hotbarWidth) / 2f;

        font.setColor(1f, 1f, 1f, 1f);

        for (int i = 0; i < Config.HOTBAR_SIZE; i++) {
            ItemStack item = player.getInventory().getSlot(i);
            if (item == null) continue;

            float slotX = startX + i * (slotSize + slotGap);

            font.draw(
                batch,
                String.valueOf(item.getQuantity()),
                slotX + slotSize - 14,
                slotY + 14
            );
        }
    }

    public void setSelectedCategory(CraftingCategory cat) {
        selectedCategory = cat;
    }

}
