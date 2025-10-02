package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Effect;

import java.util.EnumMap;
import java.util.EnumSet;

public class EffectImmuneChart {
    private static final EnumMap<Effect, EnumSet<ElementType>> ELEMENT_IMMUNITY = new EnumMap<>(Effect.class);

    static {
        ELEMENT_IMMUNITY.put(Effect.Burn,   EnumSet.of(ElementType.FIRE));
        ELEMENT_IMMUNITY.put(Effect.Wet,    EnumSet.of(ElementType.WATER));
        ELEMENT_IMMUNITY.put(Effect.Shock,  EnumSet.of(ElementType.LIGHTNING));
        ELEMENT_IMMUNITY.put(Effect.Snared, EnumSet.of(ElementType.NATURE));
    }

    public static boolean isImmuneTo(GameObject gameObject, Effect effect) {
        var blockers = ELEMENT_IMMUNITY.get(effect);
        if (blockers == null || blockers.isEmpty()) return false;

        for (ElementType b : blockers) {
            if (gameObject.getElement().has(b)) return true;
        }
        return false;
    }
}
