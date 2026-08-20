package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.ObjectSummoningMagic;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.bot.BotCounterEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

    // 적 필드에 서 있는 유닛의 값을 정하는 레시피. 봇이 낼 수 있는 2장짜리(10마나)보다 비싸야
    // "필드보다 싼 것만 낸다" 규칙이 후보를 남긴다.
    private static final List<CardType> ENEMY_BOARD_RECIPE =
            List.of(CardType.Spawn, CardType.Rock, CardType.Rock, CardType.Rock, CardType.Rock);

    private final DatabaseMagicParser magicParser = mock(DatabaseMagicParser.class);
    private final BotCounterEvaluator counterEvaluator = mock(BotCounterEvaluator.class);
    private final Parameters parameters = mock(Parameters.class);

    @BeforeEach
    void setUp() {
        when(magicParser.getAllMagicRecipes()).thenReturn(List.of(FAVOURABLE, LOSING));
        priceEnemyBoardAt(ENEMY_BOARD_RECIPE);
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

    // 핵심 규칙. 플레이어 필드가 15마나짜리 하나뿐이면 봇의 20마나 소환은 후보에서 빠지고,
    // 10마나짜리만 남는다.
    @Test
    void hospitalityOnlyCastsSummonsCheaperThanTheEnemyBoard() {
        priceEnemyBoardAt(List.of(CardType.Spawn, CardType.Rock, CardType.Rock));
        List<CardType> tooBig = List.of(CardType.Spawn, CardType.Nature, CardType.Nature, CardType.Nature);
        when(magicParser.getAllMagicRecipes()).thenReturn(List.of(tooBig, LOSING));

        BotBrain.InputDecision decision = think(hospitalityPersona());

        assertThat(decision).isNotNull();
        assertThat(decision.playCards()).isEqualTo(LOSING);
    }

    // 규칙을 지킬 수 없으면 소환하지 않는다. 데드라인이 지났어도 마찬가지다. 플레이어보다 센
    // 것을 한 번 내놓는 순간 접대는 실패하고, 그건 잠깐 조용한 것보다 나쁘다.
    @Test
    void hospitalityRefusesToSummonWhenNothingStaysUnderTheBoard() {
        priceEnemyBoardAt(List.of(CardType.Spawn));

        BotBrain brain = new BotBrain(magicParser, counterEvaluator, hospitalityPersona());
        BotBrain.InputDecision decision = brain.think(eye(), parameters, Master.LeftPlayer, true);

        assertThat(decision == null || decision.playCards().size() == 1)
                .as("summoned instead of holding or cycling")
                .isTrue();
    }

    // 일반 봇은 이 규칙과 무관하다. 이기려는 봇이 상대 필드보다 싸게만 낼 이유가 없다.
    @Test
    void ordinaryTiersIgnoreTheEnemyBoardCap() {
        priceEnemyBoardAt(List.of(CardType.Spawn));

        BotBrain.InputDecision decision = think(persona(1.0));

        assertThat(decision).isNotNull();
        assertThat(decision.playCards()).isEqualTo(FAVOURABLE);
    }

    // 스웜 하나가 다섯 마리를 내놓으면 한 마리 값은 레시피 비용의 5분의 1이다. 그러지 않으면
    // 같은 소환이 필드 위에서 다섯 배로 계산돼, 봇이 훨씬 센 것을 내도 되는 것처럼 보인다.
    @Test
    void aSwarmBodyIsWorthItsShareOfTheCast() {
        // 적 보드가 25가 아니라 5로 계산되므로 10마나짜리 소환은 규칙을 지키지 못한다.
        priceEnemyBoardAt(ENEMY_BOARD_RECIPE, 5);

        BotBrain brain = new BotBrain(magicParser, counterEvaluator, hospitalityPersona());
        BotBrain.InputDecision decision = brain.think(eye(), parameters, Master.LeftPlayer, true);

        assertThat(decision == null || decision.playCards().size() == 1).isTrue();
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

    // 적 유닛의 가격은 그 유닛을 소환하는 마법의 레시피 비용을 소환 개수로 나눈 값이다.
    private void priceEnemyBoardAt(List<CardType> recipe) {
        priceEnemyBoardAt(recipe, 1);
    }

    private void priceEnemyBoardAt(List<CardType> recipe, int quantity) {
        when(magicParser.getAllMagicRecipeMap())
                .thenReturn(Map.of(recipe, new TestSummon(PrefabType.FireSpirit, quantity)));
    }

    /** 필드에 프리팹을 남기는 마법. BoardValue가 값을 매기는 대상이다. */
    private static final class TestSummon extends Magic implements ObjectSummoningMagic {

        private final PrefabType prefab;
        private final int quantity;

        private TestSummon(PrefabType prefab, int quantity) {
            super(CardType.Spawn);
            this.prefab = prefab;
            this.quantity = quantity;
        }

        @Override
        public void run(GameContext gameContext, Master master, Vector3 position) {
        }

        @Override
        public PrefabType summonedPrefab() {
            return prefab;
        }

        @Override
        public int summonedQuantity() {
            return quantity;
        }
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
