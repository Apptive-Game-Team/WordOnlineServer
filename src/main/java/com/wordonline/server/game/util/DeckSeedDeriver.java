package com.wordonline.server.game.util;

public final class DeckSeedDeriver {

    private static final long LEFT_DECK_SALT = 0x4c454654L;
    private static final long RIGHT_DECK_SALT = 0x5249474854L;

    private DeckSeedDeriver() {
    }

    public static long forLeftDeck(long sessionSeed) {
        return derive(sessionSeed, LEFT_DECK_SALT);
    }

    public static long forRightDeck(long sessionSeed) {
        return derive(sessionSeed, RIGHT_DECK_SALT);
    }

    private static long derive(long sessionSeed, long sideSalt) {
        long seed = sessionSeed ^ sideSalt;
        seed ^= (seed >>> 33);
        seed *= 0xff51afd7ed558ccdL;
        seed ^= (seed >>> 33);
        seed *= 0xc4ceb9fe1a85ec53L;
        seed ^= (seed >>> 33);
        return seed;
    }
}
