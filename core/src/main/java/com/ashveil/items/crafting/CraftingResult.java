package com.ashveil.items.crafting;

import com.ashveil.items.inventory.ItemType;

public class CraftingResult {
    private final CraftStatus status;
    private final ItemType resultType;
    private final int resultAmount;
    private final int overflowAmount;

    public CraftingResult(CraftStatus status, ItemType resultType, int resultAmount, int overflowAmount) {
        if (status == null) throw new IllegalArgumentException("Craft status cannot be null.");
        if (resultType == null) throw new IllegalArgumentException("Craft result type cannot be null.");
        if (resultAmount < 0) throw new IllegalArgumentException("Craft result amount cannot be negative.");
        if (overflowAmount < 0 || overflowAmount > resultAmount) throw new IllegalArgumentException("Craft overflow amount is invalid.");
        if (status != CraftStatus.SUCCESS && (resultAmount != 0 || overflowAmount != 0))
            throw new IllegalArgumentException("Failed crafting cannot produce items.");

        this.status = status;
        this.resultType = resultType;
        this.resultAmount = resultAmount;
        this.overflowAmount = overflowAmount;
    }

    public boolean isSuccess() {return status == CraftStatus.SUCCESS;}

    public CraftStatus getStatus() {return status;}
    public ItemType getResultType() {return resultType;}
    public int getResultAmount() {return resultAmount;}
    public int getOverflowAmount() {return overflowAmount;}
}
