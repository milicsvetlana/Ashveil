package com.ashveil.economy;

public class Wallet {
    private int gold;

    public Wallet() {
        this.gold = 0;
    }

    public void addGold(int amount){
        if (amount < 0) return;
        gold += amount;
    }

    public boolean spendGold(int amount){
        if (amount < 0 || !canAfford(amount)) return false;
        gold -= amount;
        return true;
    }

    public boolean canAfford(int amount){
        return amount <= gold;
    }

    public int getGold() {return gold;}
}
