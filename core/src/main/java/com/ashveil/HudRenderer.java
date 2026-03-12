package com.ashveil;

import com.ashveil.entities.Player;
import com.ashveil.items.ItemStack;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

import static com.ashveil.Config.SCREEN_HEIGHT;
import static com.ashveil.Config.SCREEN_WIDTH;

public class HudRenderer {
    private ShapeRenderer shapeRenderer;

    public HudRenderer(){
        shapeRenderer = new ShapeRenderer();
    }

    public void render(Player player){
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
            shapeRenderer.end();
    }

    public void dispose(){
        shapeRenderer.dispose();
    }
}

