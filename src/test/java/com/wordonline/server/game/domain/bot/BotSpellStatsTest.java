package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BotSpellStatsTest {

    private final Parameters parameters = mock(Parameters.class);
    private final BotSpellStats spellStats = new BotSpellStats(parameters);

    @Test
    void chargesOneCastRatherThanASumOfCards() {
        when(parameters.getValueOrDefault(eq("Shoot"), eq("mana_cost"), anyDouble())).thenReturn(35.0);

        assertThat(spellStats.manaCost(magic(CardType.Shoot))).isEqualTo(35);
    }

    @Test
    void treatsAnUnpricedMagicAsUnaffordable() {
        when(parameters.getValueOrDefault(eq("Shoot"), eq("mana_cost"), anyDouble()))
                .thenAnswer(invocation -> invocation.getArgument(2));

        assertThat(spellStats.manaCost(magic(CardType.Shoot)))
                .isEqualTo(BotSpellStats.UNKNOWN_MANA_COST);
    }

    @Test
    void readsCastRangeFromTheMagic() {
        when(parameters.getValueOrDefault(eq("Explode"), eq("range"), anyDouble())).thenReturn(9.0);

        assertThat(spellStats.castRange(magic(CardType.Explode))).isEqualTo(9.0);
    }

    @Test
    void fallsBackToNeutralDamageWhenTheMagicHasNoDamageRow() {
        when(parameters.getValueOrDefault(eq("Spawn"), eq("damage"), anyDouble()))
                .thenAnswer(invocation -> invocation.getArgument(2));

        assertThat(spellStats.damagePerTarget(magic(CardType.Spawn))).isEqualTo(BotSpellStats.UNKNOWN_DAMAGE);
    }

    private static Magic magic(CardType castType) {
        return new Magic(castType) {
            @Override
            public void run(GameContext gameContext, Master master, Vector3 position) {
            }
        };
    }
}
