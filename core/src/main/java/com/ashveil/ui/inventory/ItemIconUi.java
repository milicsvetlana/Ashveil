package com.ashveil.ui.inventory;

import com.ashveil.items.inventory.ItemType;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public final class ItemIconUi {

    private ItemIconUi(){}

    public static Drawable getDrawable(Skin skin, ItemType type){
        return skin.getDrawable(getDrawableName(type));
    }

    private static String getDrawableName(ItemType type){
        return switch (type){
            case WOOD -> "item-wood";
            case STONE -> "item-stone";
            case WHEAT -> "item-wheat";
            case WHEAT_SEED -> "item-wheat-seed";
            case BREAD -> "item-bread";
            case SAPLING -> "item-sapling";
            case HOLLOWCAP -> "item-hollowcap";

            case WOODEN_AXE -> "item-wooden-axe";
            case STONE_AXE -> "item-stone-axe";
            case WOODEN_PICKAXE -> "item-wooden-pickaxe";
            case STONE_PICKAXE -> "item-stone-pickaxe";
            case WOODEN_HOE -> "item-wooden-hoe";
            case STONE_HOE -> "item-stone-hoe";

            case WOODEN_SWORD -> "item-wooden-sword";
            case STONE_SWORD -> "item-stone-sword";
            case BLOODTHIRST_SWORD -> "item-bloodthirst-sword";

            case FENCE -> "item-fence";
            case THORN_FENCE -> "item-thorn-fence";
            case BRIAR_SNARE -> "item-briar-snare";
            case CHEST -> "item-chest";

            case BOAT_KIT -> "item-boat-kit";

            case SCROLL_I -> "item-scroll-1";
            case SCROLL_II -> "item-scroll-2";
            case SCROLL_III -> "item-scroll-3";

            case GOLD -> "item-gold";
            case HEART_REPAIR -> "item-heart-repair";
        };
    }
}
