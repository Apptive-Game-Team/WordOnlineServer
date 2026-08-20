package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotTier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CastCadenceTest {

    private static final long START = 1_000_000L;

    // The floor. Without it the bot summons on every tick its mana allows, and the board refills
    // faster than a new player can answer - the opponent built to lose wins comfortably.
    @Test
    void holdsTheHospitalityBotToOneCastPerInterval() {
        CastCadence cadence = CastCadence.forTier(BotTier.HOSPITALITY, START);
        cadence.recordCast(START);

        assertThat(cadence.due(START + CastCadence.HOSPITALITY_INTERVAL_MILLIS - 1)).isFalse();
    }

    // The ceiling. A still opponent reads as one going easy, so once the interval is up the bot
    // acts whether or not it likes its options.
    @Test
    void makesTheHospitalityBotDueOnceTheIntervalElapses() {
        CastCadence cadence = CastCadence.forTier(BotTier.HOSPITALITY, START);
        cadence.recordCast(START);

        assertThat(cadence.due(START + CastCadence.HOSPITALITY_INTERVAL_MILLIS)).isTrue();
    }

    // The clock starts at session start, not at the first cast: an opening that goes quiet reads
    // exactly the same as a mid-game one.
    @Test
    void countsTheSilenceBeforeTheFirstCastToo() {
        CastCadence cadence = CastCadence.forTier(BotTier.HOSPITALITY, START);

        assertThat(cadence.due(START)).isFalse();
        assertThat(cadence.due(START + CastCadence.HOSPITALITY_INTERVAL_MILLIS)).isTrue();
    }

    @Test
    void leavesEveryOtherTierUnpaced() {
        for (BotTier tier : BotTier.values()) {
            if (tier == BotTier.HOSPITALITY) {
                continue;
            }
            CastCadence cadence = CastCadence.forTier(tier, START);
            cadence.recordCast(START);

            assertThat(cadence.due(START)).as("tier %s", tier).isTrue();
            assertThat(cadence.paced()).as("tier %s", tier).isFalse();
        }
    }

    @Test
    void reportsTheHospitalityBotAsPaced() {
        assertThat(CastCadence.forTier(BotTier.HOSPITALITY, START).paced()).isTrue();
    }
}
