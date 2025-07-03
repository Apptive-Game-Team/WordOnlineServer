package com.wordonline.server.game.domain.magic;

public abstract class ElementalChart {
    // row: ATK, col: DEF
    private static final float[][] CHART = {
            // DEF    ATK  NONE   FIRE   GRASS  WATER  LIGHTING  ROCK
            /* NONE    */ { 1f,    1f,    1f,    1f,    1f,       1f    },
            /* FIRE    */ { 1f,    1f,  0.5f,    2f,    1f,       2f    },
            /* GRASS   */ { 1f,    2f,    1f,  0.5f,    2f,     0.5f    },
            /* WATER   */ { 1f,  0.5f,    1f,    1f,    2f,     0.5f    },
            /* LIGHTING*/ { 1f,    1f,    1f,    1f,    1f,       1f    },
            /* ROCK    */ { 1f,  0.5f,    2f,    2f,  0.5f,       2f    },
    };

    public static float getMultiplier(ElementType atk, ElementType def) {
        return CHART[atk.ordinal()][def.ordinal()];
    }
}
