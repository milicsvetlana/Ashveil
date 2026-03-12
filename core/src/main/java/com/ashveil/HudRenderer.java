package com.ashveil;

import com.ashveil.entities.Player;
import com.ashveil.items.ItemStack;
import com.ashveil.world.DayNightCycle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

import static com.ashveil.Config.SCREEN_HEIGHT;
import static com.ashveil.Config.SCREEN_WIDTH;

public class HudRenderer {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    public HudRenderer(){
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    public void render(Player player, DayNightCycle dayNightCycle){
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
    }

    public void dispose(){
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }
}

