package com.wordonline.server.game.domain.magic;

import java.util.Collection;
import java.util.List;

public abstract class ElementalChart {
    // row: ATK, col: DEF
    private static final float[][] CHART = {
            // DEF    ATK  NONE   FIRE   NATURE  WATER  LIGHTNING  ROCK   WIND
            /* NONE    */ { 1f,    1f,    1f,    1f,    1f,       1f,   1f    },
            /* FIRE    */ { 1f,    1f,  0.5f,    2f,    1f,       1f,   1f    },
            /* NATURE   */ { 1f,    2f,    1f,  0.5f,    1f,       1f,   1f    },
            /* WATER   */ { 1f,  0.5f,    2f,    1f,    2f,     0.5f,   1f    },
            /* LIGHTNING*/ { 1f,    1f,    1f,    1f,    1f,       1f,   1f    },
            /* ROCK    */ { 1f,  0.5f,    1.5f, 1.5f,  0.5f,       1.5f,   0.5f    },
            /* WIND    */ { 1f,    1f,    1f,    1f,    2f,       2f,   1f    },

    };

    public static float getMultiplier(ElementType atk, ElementType def) {
        return CHART[atk.ordinal()][def.ordinal()];
    }

    public static float computePairwiseProductMultiplier(
            Collection<ElementType> atkElements,
            Collection<ElementType> defElements
    ) {
        if (atkElements == null || atkElements.isEmpty())
            atkElements = List.of(ElementType.NONE);
        if (defElements == null || defElements.isEmpty())
            defElements = List.of(ElementType.NONE);

        float product = 1f;
        for (ElementType atk : atkElements) {
            for (ElementType def : defElements) {
                float m = getMultiplier(atk, def);
                product *= m;
            }
        }
        return product;
    }
}
