package com.wordonline.server.game.domain;

import lombok.Getter;
import lombok.Setter;

public class Stat {
    private final float originalStat;
    @Getter @Setter
    private float modifierPercent = 0f;

    public void addPercent(float deltaPercent) {
        modifierPercent += deltaPercent;
    }

    public int getFloorValue() {
        return (int) getFinalValue();
    }

    public float getFinalValue()
    {
        return originalStat * (1 + modifierPercent);
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
