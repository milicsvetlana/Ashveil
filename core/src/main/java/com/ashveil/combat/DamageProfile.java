package com.ashveil.combat;

import java.util.Map;

public final class DamageProfile {
    private final int baseDamage;
    private final Map<HitCategory, Integer> categoryDamage;

    public DamageProfile(int baseDamage, Map<HitCategory, Integer> categoryDamage) {
        if (baseDamage < 0) throw new IllegalArgumentException("Base damage cannot be negative.");
        if (categoryDamage == null) throw new IllegalArgumentException("Map can't be null");
        this.baseDamage = baseDamage;
        this.categoryDamage = Map.copyOf(categoryDamage); //mapa kako neko ne bi spolja mogao naknadno promeniti mapu
    }

    public int getDamage(HitCategory category){
        if (category == null) throw new IllegalArgumentException("Category can't be null");
        Integer damage = categoryDamage.get(category);
        if (damage == null){
            damage = baseDamage;
        }
        return damage;
    }

    public int getBaseDamage() {return baseDamage;}
}
