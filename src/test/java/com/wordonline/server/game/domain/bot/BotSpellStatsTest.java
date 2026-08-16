package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.CardType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BotSpellStatsTest {

    private final Parameters parameters = mock(Parameters.class);
    private final BotSpellStats spellStats = new BotSpellStats(parameters);

    @Test
    void chargesEveryCardInTheRecipeNotOnlyTheMagicCard() {
        manaCost("Shoot", 15);
        manaCost("Fire", 10);
        manaCost("Rock", 10);

        assertThat(spellStats.totalManaCost(List.of(CardType.Shoot, CardType.Fire, CardType.Rock)))
                .isEqualTo(35);
    }

    @Test
    void treatsRecipeWithUnpricedCardAsUnaffordable() {
        manaCost("Shoot", 15);
        when(parameters.getValueOrDefault(eq("Fire"), eq("mana_cost"), anyDouble()))
                .thenAnswer(invocation -> invocation.getArgument(2));

        assertThat(spellStats.totalManaCost(List.of(CardType.Shoot, CardType.Fire)))
                .isEqualTo(BotSpellStats.UNKNOWN_MANA_COST);
    }

    @Test
    void readsCastRangeFromTheMagicCard() {
        when(parameters.getValueOrDefault(eq("Explode"), eq("range"), anyDouble())).thenReturn(9.0);

        assertThat(spellStats.castRange(CardType.Explode)).isEqualTo(9.0);
    }

    private void manaCost(String card, double cost) {
        when(parameters.getValueOrDefault(eq(card), eq("mana_cost"), anyDouble())).thenReturn(cost);
    }
}
