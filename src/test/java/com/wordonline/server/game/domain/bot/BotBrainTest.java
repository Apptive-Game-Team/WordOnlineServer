package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.bot.BotCounterEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The sign of {@code counterAggression} decides which direction of the matchup the brain scores.
 * Both recipes here cost the same and place the same number of cards, so the counter term is the
 * only thing that can separate them.
 */
class BotBrainTest {

    private static final List<CardType> FAVOURABLE = List.of(CardType.Spawn, CardType.Fire);
    private static final List<CardType> LOSING = List.of(CardType.Spawn, CardType.Water);

    private final DatabaseMagicParser magicParser = mock(DatabaseMagicParser.class);
    private final BotCounterEvaluator counterEvaluator = mock(BotCounterEvaluator.class);
    private final Parameters parameters = mock(Parameters.class);

    @BeforeEach
    void setUp() {
        when(magicParser.getAllMagicRecipes()).thenReturn(List.of(FAVOURABLE, LOSING));
        when(parameters.getValueOrDefault(anyString(), anyString(), anyDouble()))
                .thenAnswer(invocation -> invocation.getArgument(2));
        when(parameters.getValueOrDefault(anyString(), eq("mana_cost"), anyDouble())).thenReturn(5.0);

        when(counterEvaluator.evaluate(eq(FAVOURABLE), any())).thenReturn(10.0);
        when(counterEvaluator.evaluate(eq(LOSING), any())).thenReturn(0.0);
        when(counterEvaluator.evaluateVulnerability(eq(FAVOURABLE), any())).thenReturn(0.0);
        when(counterEvaluator.evaluateVulnerability(eq(LOSING), any())).thenReturn(10.0);
    }

    @Test
    void positiveAggressionPicksThePlayThatBeatsTheField() {
        BotBrain.InputDecision decision = think(persona(1.0));

        assertThat(decision).isNotNull();
        assertThat(decision.playCards()).isEqualTo(FAVOURABLE);
    }

    @Test
    void negativeAggressionPicksThePlayTheFieldAnswersBest() {
        BotBrain.InputDecision decision = think(persona(-1.0));

        assertThat(decision).isNotNull();
        assertThat(decision.playCards()).isEqualTo(LOSING);
    }

    // A bot that is meant to lose still commits a unit every time. Choosing nothing, or choosing
    // something irrelevant, is what reads to the player as being humoured.
    @Test
    void negativeAggressionStillCasts() {
        assertThat(think(persona(-1.0))).isNotNull();
    }

    @Test
    void neutralAggressionAsksForNeitherDirection() {
        think(persona(0.0));

        verify(counterEvaluator, never()).evaluate(any(), any());
        verify(counterEvaluator, never()).evaluateVulnerability(any(), any());
    }

    // The hospitality bot's deck cannot assemble anything but a summon, but a deck constrains
    // which cards are drawn, not how many end up in one recipe. These two cases are what the code
    // filter is for.
    @Test
    void hospitalityNeverCastsANonSummon() {
        when(magicParser.getAllMagicRecipes())
                .thenReturn(List.of(List.of(CardType.Shoot, CardType.Fire), LOSING));

        BotBrain.InputDecision decision = think(hospitalityPersona());

        assertThat(decision).isNotNull();
        assertThat(decision.playCards()).isEqualTo(LOSING);
    }

    @Test
    void hospitalityNeverCastsASummonBiggerThanThreeCards() {
        List<CardType> bigSummon = List.of(CardType.Spawn, CardType.Fire, CardType.Water, CardType.Rock);
        when(magicParser.getAllMagicRecipes()).thenReturn(List.of(bigSummon, LOSING));

        BotBrain.InputDecision decision = think(hospitalityPersona());

        assertThat(decision).isNotNull();
        assertThat(decision.playCards()).isEqualTo(LOSING);
    }

    // Its stored aggression is overridden per cast by the board state, so an unfavourable persona
    // value cannot make it start playing to win.
    @Test
    void hospitalityPrefersThePlayTheFieldAnswersEvenWithAPositivePersonaValue() {
        BotBrain.InputDecision decision = think(
                new BotPersona(-1, "Host", BotTier.HOSPITALITY, 0, 1, 1.0, true, true));

        assertThat(decision).isNotNull();
        assertThat(decision.playCards()).isEqualTo(LOSING);
    }

    private BotPersona hospitalityPersona() {
        return new BotPersona(-1, "Host", BotTier.HOSPITALITY, 0, 1, -1.0, true, true);
    }

    // Holding for mana is right for a bot that is trying to win and wrong for one that has been
    // quiet too long: the player reads the pause, not the reason for it.
    // 60 a card against 100 mana: no two-card recipe is affordable, but one card on its own is.
    @Test
    void holdsForManaWhenItIsNotOverdue() {
        when(parameters.getValueOrDefault(anyString(), eq("mana_cost"), anyDouble())).thenReturn(60.0);

        assertThat(think(hospitalityPersona())).isNull();
    }

    @Test
    void spendsACardToCycleRatherThanStandStillWhenOverdue() {
        when(parameters.getValueOrDefault(anyString(), eq("mana_cost"), anyDouble())).thenReturn(60.0);

        BotBrain brain = new BotBrain(magicParser, counterEvaluator, hospitalityPersona());
        BotBrain.InputDecision decision = brain.think(eye(), parameters, Master.LeftPlayer, true);

        assertThat(decision).isNotNull();
        assertThat(decision.playCards()).hasSize(1);
    }

    private BotBrain.InputDecision think(BotPersona persona) {
        BotBrain brain = new BotBrain(magicParser, counterEvaluator, persona);
        return brain.think(eye(), parameters, Master.LeftPlayer);
    }

    private static BotEye eye() {
        return new BotEye(
                List.of(BotBrainTestFixtures.enemyUnit()),
                List.of(CardType.Spawn, CardType.Fire, CardType.Water, CardType.Rock, CardType.Shoot),
                100,
                100);
    }

    // ELITE keeps explorationRate at 0, so the ranking is never thrown away and the assertion is
    // about the score rather than about a coin flip.
    private static BotPersona persona(double counterAggression) {
        return new BotPersona(-1, "Test", BotTier.ELITE, 0, 1, counterAggression, true, false);
    }
}
