package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotTier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CastPacerTest {

    private static final long NOW = 1_000_000L;

    @Test
    void holdsTheHospitalityBotToOneCastPerInterval() {
        CastPacer pacer = CastPacer.forTier(BotTier.HOSPITALITY);

        assertThat(pacer.allows(NOW)).isTrue();
        pacer.recordCast(NOW);

        assertThat(pacer.allows(NOW + CastPacer.HOSPITALITY_INTERVAL_MILLIS - 1)).isFalse();
    }

    // The interval is a floor, not a budget: once it elapses the bot casts again, whatever the
    // score looks like. Going quiet is what reads to the player as being humoured.
    @Test
    void letsTheHospitalityBotCastAgainOnceTheIntervalElapses() {
        CastPacer pacer = CastPacer.forTier(BotTier.HOSPITALITY);
        pacer.recordCast(NOW);

        assertThat(pacer.allows(NOW + CastPacer.HOSPITALITY_INTERVAL_MILLIS)).isTrue();
    }

    @Test
    void leavesEveryOtherTierUnpaced() {
        for (BotTier tier : BotTier.values()) {
            if (tier == BotTier.HOSPITALITY) {
                continue;
            }
            CastPacer pacer = CastPacer.forTier(tier);
            pacer.recordCast(NOW);

            assertThat(pacer.allows(NOW)).as("tier %s", tier).isTrue();
        }
    }
}
