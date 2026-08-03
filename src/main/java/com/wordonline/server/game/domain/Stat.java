package com.wordonline.server.game.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

public class Stat {
    private final float originalStat;
    @Getter @Setter
    private float modifierPercent = 0f;
    private final Map<Object, Float> multipliers = new LinkedHashMap<>();

    public void addPercent(float deltaPercent) {
        modifierPercent += deltaPercent;
    }

    public int getFloorValue() {
        return (int) total();
    }

    public void setMultiplier(Object source, float multiplier) {
        multipliers.put(source, multiplier);
    }

    public void removeMultiplier(Object source) {
        multipliers.remove(source);
    }

    public float total()
    {
        float total = originalStat * (1 + modifierPercent);
        for (float multiplier : multipliers.values()) {
            total *= multiplier;
        }
        return total;
    }

    public Stat(float originalStat)
    {
        this.originalStat = originalStat;
    }

    public Stat(float originalStat, float modifierPercent)
    {
        this.originalStat = originalStat;
        this.modifierPercent = modifierPercent;
    }
}
