package com.ashveil.combat;

import com.ashveil.Config;
import com.ashveil.entities.Player;
import com.ashveil.items.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CombatSystem {

    public int performPrimaryAction(Player player, List<Hittable> targets) {
        if (player == null || targets == null) return 0;

        int numberOfHits = 0;
        ItemStack activeItem = player.getInventory().getSlot(player.getSelectedHotbarSlot());
        boolean usesDurability = activeItem != null && activeItem.getType().usesDurability();

        List<Hittable> inRange = new ArrayList<>();

        for (Hittable target : targets) {
            if (!target.canReceiveHit()) continue;
            if (!isTargetInFrontCone(player, target.getCenterX(), target.getCenterY(), Config.PLAYER_PRIMARY_ACTION_RANGE,
                Config.PLAYER_PRIMARY_ACTION_MIN_DOT)) continue;

            inRange.add(target);
        }

        inRange.sort((first, second) -> Float.compare(
            getDistanceSquared(player, first),
            getDistanceSquared(player, second)
        ));

        for (Hittable target : inRange){
            if (usesDurability && activeItem.getDurability() <= 0) break;

            int damage;

            if (usesDurability){
                damage = activeItem.getType().getDamageProfile().getDamage(target.getHitCategory());
            }
            else{
                damage = getHandDamage(target.getHitCategory());
            }

            target.receiveHit(damage);
            numberOfHits++;

            if (usesDurability){
                boolean broken = activeItem.reduceDurability(1);

                if (broken){
                    player.getInventory().removeFromSlot(player.getSelectedHotbarSlot(), 1);
                    break;
                }
            }
        }

        return numberOfHits;
    }

    private float getDistanceSquared(Player player, Hittable target) {
        float dx = target.getCenterX() - player.getCenterX();
        float dy = target.getCenterY() - player.getCenterY();

        return dx * dx + dy * dy;
    }

    private boolean isTargetInFrontCone(Player player, float targetCenterX, float targetCenterY, float range, float minDot) {
        float dx = targetCenterX - player.getCenterX();
        float dy = targetCenterY - player.getCenterY();

        float distSq = dx * dx + dy * dy;
        if (distSq > range * range) return false;
        if (distSq == 0f) return true;

        float invLen = 1f / (float)Math.sqrt(distSq);
        dx *= invLen;
        dy *= invLen;

        float dot = dx * player.getFacingX() + dy * player.getFacingY();
        return dot >= minDot;
    }

    private int getHandDamage(HitCategory hitCategory){
        if (hitCategory == HitCategory.STONE) return 0;
        return Config.PLAYER_BASE_DAMAGE;
    }

}
