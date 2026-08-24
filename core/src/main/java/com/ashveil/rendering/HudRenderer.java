package com.ashveil.rendering;

import com.ashveil.Config;
import com.ashveil.entities.Player;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.items.inventory.ItemType;
import com.ashveil.world.DayNightCycle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.ashveil.Config.SCREEN_HEIGHT;
import static com.ashveil.Config.SCREEN_WIDTH;

public class HudRenderer {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera hudCamera;
    private Viewport hudViewport;

    public HudRenderer(){
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        hudCamera = new OrthographicCamera();
        hudViewport = new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, hudCamera);
    }

    public void render(Player player, DayNightCycle dayNightCycle){
        hudViewport.apply();
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        batch.setProjectionMatrix(hudCamera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawHearts(player);
        drawGoldIcon();
        drawHotbar(player);
        drawDayNightIcon(dayNightCycle);
        shapeRenderer.end();

        batch.begin();
        drawDayText(dayNightCycle);
        drawGoldText(player);
        drawHotbarText(player);
        drawFps();
        batch.end();
    }

    public void dispose(){
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }

    private void drawHearts(Player player){
        int hp = player.getCurrentHp();
        int brokenHearts = player.getBrokenHearts();

        int heartSize = 20;
        int heartGap = 5;
        int startX = 10;
        float y = hudViewport.getWorldHeight() - 30;

        int firstBrokenHeart = Config.PLAYER_HEART_SLOTS - brokenHearts;

        for (int i = 0; i < Config.PLAYER_HEART_SLOTS; i++) {
            float x = startX + i * (heartSize + heartGap);

            if (i >= firstBrokenHeart) {
                shapeRenderer.setColor(1f, 1f, 1f, 1f);
                shapeRenderer.rect(x, y, heartSize, heartSize);
                continue;
            }

            int hpInHeart = hp - i * Config.HP_PER_HEART;

            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
            shapeRenderer.rect(x, y, heartSize, heartSize);

            if (hpInHeart >= Config.HP_PER_HEART) {
                shapeRenderer.setColor(1f, 0f, 0f, 1f);
                shapeRenderer.rect(x, y, heartSize, heartSize);
            }
            else if (hpInHeart == 1) {
                shapeRenderer.setColor(1f, 0f, 0f, 1f);
                shapeRenderer.rect(x, y, heartSize / 2f, heartSize);
            }
        }
    }

    private void drawGoldIcon(){
        float x = 20f;
        float y = hudViewport.getWorldHeight() - 55f;

        shapeRenderer.setColor(1f, 0.78f, 0.12f, 1f);
        shapeRenderer.circle(x, y, 7f);
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

    private void drawFps(){
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10f, hudViewport.getWorldHeight() - 80f);
    }

    private void drawGoldText(Player player){
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, String.valueOf(player.getWallet().getGold()), 33f, hudViewport.getWorldHeight() - 49f);
    }

    public void resize(int width, int height){
        hudViewport.update(width, height, true);
    }

    private void setTemporaryItemColor(ItemType type) {
        switch (type) {
            case WOOD -> shapeRenderer.setColor(0.45f, 0.25f, 0.1f, 1f);
            case STONE -> shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
            case WHEAT, BREAD -> shapeRenderer.setColor(0.9f, 0.75f, 0.2f, 1f);
            case WHEAT_SEED -> shapeRenderer.setColor(0.2f, 0.65f, 0.2f, 1f);
            case WOODEN_AXE, WOODEN_PICKAXE, WOODEN_HOE, WOODEN_SWORD -> shapeRenderer.setColor(0.65f, 0.25f, 0.15f, 1f);
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

}
