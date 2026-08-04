package com.ashveil.items.crafting;

import com.ashveil.items.inventory.ItemType;

public interface CraftingAccess {
    CraftStatus getCraftStatus(String recipeId);
    CraftingResult tryCraft(String recipeId);
    int getOwnedQuantity(ItemType itemType);
}

