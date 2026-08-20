package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotTier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CastDeadlineTest {

    private static final long START = 1_000_000L;

    // The deadline caps silence, it does not pace casting. A bot that just cast is free to cast
    // again on the very next tick; difficulty comes from what it summons, not how often.
    @Test
    void neverStandsBetweenTwoCasts() {
        CastDeadline deadline = CastDeadline.forTier(BotTier.HOSPITALITY, START);
        deadline.recordCast(START);

        assertThat(deadline.overdue(START + 1)).isFalse();
    }

    @Test
    void forcesTheHospitalityBotToActOnceItHasBeenQuietTooLong() {
        CastDeadline deadline = CastDeadline.forTier(BotTier.HOSPITALITY, START);

        assertThat(deadline.overdue(START + CastDeadline.HOSPITALITY_MAX_SILENCE_MILLIS - 1)).isFalse();
        assertThat(deadline.overdue(START + CastDeadline.HOSPITALITY_MAX_SILENCE_MILLIS)).isTrue();
    }

    // The clock starts at session start, not at the first cast: an opening that goes quiet reads
    // exactly the same as a mid-game one.
    @Test
    void countsTheSilenceBeforeTheFirstCastToo() {
        CastDeadline deadline = CastDeadline.forTier(BotTier.HOSPITALITY, START);

        assertThat(deadline.overdue(START + CastDeadline.HOSPITALITY_MAX_SILENCE_MILLIS)).isTrue();
    }

    @Test
    void castingResetsTheDeadline() {
        CastDeadline deadline = CastDeadline.forTier(BotTier.HOSPITALITY, START);
        long overdueAt = START + CastDeadline.HOSPITALITY_MAX_SILENCE_MILLIS;
        deadline.recordCast(overdueAt);

        assertThat(deadline.overdue(overdueAt)).isFalse();
    }

    @Test
    void leavesEveryOtherTierWithoutADeadline() {
        for (BotTier tier : BotTier.values()) {
            if (tier == BotTier.HOSPITALITY) {
                continue;
            }
            CastDeadline deadline = CastDeadline.forTier(tier, START);

            assertThat(deadline.overdue(START + 10 * CastDeadline.HOSPITALITY_MAX_SILENCE_MILLIS))
                    .as("tier %s", tier)
                    .isFalse();
        }
    }
}
