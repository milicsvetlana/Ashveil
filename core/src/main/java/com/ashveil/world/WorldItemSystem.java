package com.ashveil.world;

import com.ashveil.Config;
import com.ashveil.entities.Player;
import com.ashveil.items.inventory.ItemType;

import java.util.ArrayList;
import java.util.List;

public class WorldItemSystem {
    private final List<WorldItem> items;

    public WorldItemSystem(){
        items = new ArrayList<>();
    }

    public void update(float delta){
        for (WorldItem item : items) item.update(delta);
        items.removeIf(WorldItem::shouldDespawn);
    }

    public void add(WorldItem newItem){
        if (newItem.getAmount() <= 0) return;
        int remaining = newItem.getAmount();

        if (newItem.getType().isStackable()){
            for (WorldItem item : items){
                if (newItem.getType() != item.getType()) continue;

                float dimX = item.getX() - newItem.getX();
                float dimY = item.getY() - newItem.getY();
                double dist = Math.sqrt(dimX * dimX + dimY * dimY);

                if (dist > Config.WORLD_ITEM_MERGE_RANGE) continue;

                int previousRemaining = remaining;
                remaining = item.addAmount(remaining);

                if (remaining < previousRemaining) item.resetLifetime();

                if (remaining == 0) return;
            }

        }
        if (remaining != newItem.getAmount()) {
            newItem.setAmount(remaining);
        }
        items.add(newItem);
        checkSafetyLimit();
    }

    public void replaceItems(List<WorldItem> restoredItems){
        if (restoredItems == null) throw new IllegalArgumentException("Restored world items cannot be null.");
        for (WorldItem item : restoredItems){
            if (item == null) throw new IllegalArgumentException("Restored world item cannot be null.");
        }
        items.clear();
        items.addAll(restoredItems);
    }

    public boolean tryPickUpNearest(Player player){
        if (player == null) throw new IllegalArgumentException("Player cannot be null.");

        WorldItem nearestItem = findNearestItem(player.getX(), player.getY(), Config.PLAYER_PICKUP_RANGE);

        if(nearestItem == null) return false;
        if (nearestItem.getType() == ItemType.GOLD){
            player.getWallet().addGold(nearestItem.getAmount());
            items.remove(nearestItem);
            return true;
        }
        int remaining = player.getInventory().addStack(nearestItem.getStack());
        if (remaining == 0) items.remove(nearestItem);
        return true;
    }

    private WorldItem findNearestItem(float x, float y, float range){
        WorldItem nearestItem = null;
        Double nearestDistanceSquared = null;

        float rangeSquared = range * range;

        for (WorldItem item : items){
            float dimX = item.getX() - x;
            float dimY = item.getY() - y;

            double distanceSquared = dimX * dimX + dimY * dimY;
            if (distanceSquared > rangeSquared) continue;
            if (nearestDistanceSquared == null || distanceSquared < nearestDistanceSquared){
                nearestDistanceSquared = distanceSquared;
                nearestItem = item;
            }
        }

        return nearestItem;
    }

    private void checkSafetyLimit(){
        if (items.size() <= Config.WORLD_MAX_NUMBER_OF_ITEMS) return;

        for (int i=0; i < items.size(); i++){
            WorldItem item = items.get(i);

            if (item.getType().despawnsOnGround()){
                items.remove(i);
                return;
            }
        }
    }

    public List<WorldItem> getItems(){return items;}
}
