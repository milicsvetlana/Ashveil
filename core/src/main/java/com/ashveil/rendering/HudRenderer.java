package com.ashveil.rendering;

import com.ashveil.Config;
import com.ashveil.entities.Player;
import com.ashveil.items.inventory.ItemStack;
import com.ashveil.ui.inventory.ItemIconUi;
import com.ashveil.world.DayNightCycle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.ashveil.Config.SCREEN_HEIGHT;
import static com.ashveil.Config.SCREEN_WIDTH;

public class HudRenderer {

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final OrthographicCamera hudCamera;
    private final Viewport hudViewport;
    private final Skin skin;

    private final Drawable heartEmpty;
    private final Drawable heartBroken;
    private final Drawable goldCoin;

    private final Drawable clockDial;
    private final Drawable clockCrimsonOverlay;

    private final Drawable hotbarSlot;
    private final Drawable hotbarSlotSelected;

    private final TextureRegion heartFullRegion;
    private final TextureRegion heartHalfRegion;
    private final TextureRegion clockNeedleRegion;

    public HudRenderer(Skin skin){
        if (skin == null) throw new IllegalArgumentException("Skin cannot be null.");

        this.skin = skin;

        batch = new SpriteBatch();
        font = skin.getFont("hud-font");

        hudCamera = new OrthographicCamera();
        hudViewport = new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, hudCamera);

        heartEmpty = skin.getDrawable("hud-heart-empty");
        heartBroken = skin.getDrawable("hud-heart-broken");
        goldCoin = skin.getDrawable("hud-gold-coin");

        clockDial = skin.getDrawable("hud-clock-dial");
        clockCrimsonOverlay = skin.getDrawable("hud-clock-crimson");

        hotbarSlot = skin.getDrawable("hud-hotbar-slot");
        hotbarSlotSelected = skin.getDrawable("hud-hotbar-slot-selected");

        Texture heartFullTexture = skin.get("hud-heart-full", Texture.class);
        heartFullRegion = new TextureRegion(heartFullTexture);

        heartHalfRegion = new TextureRegion(heartFullTexture, 0, 0,
            heartFullTexture.getWidth() / 2, heartFullTexture.getHeight()
        );

        Texture clockNeedleTexture = skin.get("hud-clock-needle", Texture.class);
        clockNeedleRegion = new TextureRegion(clockNeedleTexture);
    }

    public void render(Player player, DayNightCycle dayNightCycle, boolean crimsonVeilActive){
        hudViewport.apply();
        batch.setProjectionMatrix(hudCamera.combined);

        batch.begin();

        drawClock(dayNightCycle, crimsonVeilActive);
        drawHearts(player);
        drawGold(player);
        drawHotbar(player);
        drawFps();

        batch.end();
    }

    private void drawHearts(Player player){
        int totalHearts = Config.PLAYER_HEART_SLOTS;
        int brokenHearts = player.getBrokenHearts();

        float heartSize = 29f;
        float gap = 2f;
        float rightMargin = 16f;

        float totalWidth = totalHearts * heartSize + (totalHearts - 1) * gap;
        float startX = hudViewport.getWorldWidth() - rightMargin - totalWidth;
        float y = hudViewport.getWorldHeight() - 200f;

        for (int i = 0; i < totalHearts; i++){
            float x = startX + i * (heartSize + gap);

            boolean broken = i >= totalHearts - brokenHearts;
            if (broken){
                heartBroken.draw(batch, x, y, heartSize, heartSize);
                continue;
            }

            heartEmpty.draw(batch, x, y, heartSize, heartSize);

            int hpInHeart = player.getCurrentHp() - i * Config.HP_PER_HEART;
            if (hpInHeart >= Config.HP_PER_HEART) batch.draw(heartFullRegion, x, y, heartSize, heartSize);
            else if (hpInHeart > 0) batch.draw(heartHalfRegion, x, y, heartSize / 2f, heartSize);
        }
    }

    private void drawGold(Player player){
        float coinSize = 28f;

        float x = hudViewport.getWorldWidth() - 70f;
        float y = hudViewport.getWorldHeight() - 167f;

        goldCoin.draw(batch, x, y, coinSize, coinSize);

        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, String.valueOf(player.getWallet().getGold()), x + coinSize + 10f, y + 21f);
    }

    private void drawClock(
        DayNightCycle dayNightCycle,
        boolean crimsonVeilActive
    ){
        float clockSize = 120f;
        float rightMargin = 26f;
        float topMargin = 12f;

        float x = hudViewport.getWorldWidth() - clockSize - rightMargin;
        float y = hudViewport.getWorldHeight() - clockSize - topMargin;

        clockDial.draw(batch, x, y, clockSize, clockSize);

        if (crimsonVeilActive) clockCrimsonOverlay.draw(batch, x, y, clockSize, clockSize);

        float rotation = getClockRotation(dayNightCycle);

        batch.draw(clockNeedleRegion, x, y, clockSize / 2f, clockSize / 2f, clockSize, clockSize, 1f, 1f, rotation);
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, "DAY " + dayNightCycle.getDayCount(), x + 0f, y - 14f);
    }

    private float getClockRotation(DayNightCycle dayNightCycle){
        float progress = dayNightCycle.getPhaseProgress();
        return switch (dayNightCycle.getDayPhase()){
            case DAY -> 55f + (-110f - 55f) * progress;
            case DUSK -> -110f + (-180f + 110f) * progress;
            case NIGHT -> -180f + (-305f + 180f) * progress;
        };
    }

    private void drawHotbar(Player player){
        float slotSize = 40f;
        float slotGap = 14f;
        float slotY = 10f;

        float frameSize = 68f;
        float frameOffset = (frameSize - slotSize) / 2f;

        float hotbarWidth = Config.HOTBAR_SIZE * slotSize + (Config.HOTBAR_SIZE - 1) * slotGap;
        float startX = (hudViewport.getWorldWidth() - hotbarWidth) / 2f;

        // PRVO crtamo samo velike okvire
        for (int i = 0; i < Config.HOTBAR_SIZE; i++){
            float slotX = startX + i * (slotSize + slotGap);

            Drawable slotDrawable = i == player.getSelectedHotbarSlot() ? hotbarSlotSelected : hotbarSlot;
            slotDrawable.draw(batch, slotX - frameOffset, slotY - frameOffset, frameSize, frameSize);
        }

        // ONDA crtamo postojeće iteme BEZ PROMENE
        for (int i = 0; i < Config.HOTBAR_SIZE; i++){
            ItemStack item = player.getInventory().getSlot(i);
            if (item == null) continue;

            float slotX = startX + i * (slotSize + slotGap);
            float padding = 4f;
            float iconSize = slotSize - padding * 2f;

            Drawable icon = ItemIconUi.getDrawable(skin, item.getType());

            icon.draw(batch, slotX + padding, slotY + padding, iconSize, iconSize);

            if (item.getQuantity() > 1){
                font.setColor(1f, 1f, 1f, 1f);
                font.draw(batch, String.valueOf(item.getQuantity()), slotX + slotSize - 14f, slotY + 14f);
            }
        }
    }

    private void drawFps(){
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(),
            hudViewport.getWorldWidth() - 70f, 24f);
    }

    public void resize(int width, int height){
        hudViewport.update(width, height, true);
    }

    public void dispose(){
        batch.dispose();
    }
}
