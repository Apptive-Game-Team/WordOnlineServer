package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.BotWorldView;
import com.wordonline.server.game.domain.bot.view.ObservedObject;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Status;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.BOT_SIDE;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.ENEMY_SIDE;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.SELF;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.cluster;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.object;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.view;
import static com.wordonline.server.game.domain.bot.rule.BotRules.ally;
import static com.wordonline.server.game.domain.bot.rule.BotRules.anyUnit;
import static com.wordonline.server.game.domain.bot.rule.BotRules.enemy;
import static com.wordonline.server.game.domain.bot.rule.BotRules.enemyCluster;
import static com.wordonline.server.game.domain.bot.rule.BotRules.handContains;
import static com.wordonline.server.game.domain.bot.rule.BotRules.manaAtLeast;
import static com.wordonline.server.game.domain.bot.rule.BotRules.not;
import static com.wordonline.server.game.domain.bot.rule.BotRules.pressureAtLeast;
import static org.assertj.core.api.Assertions.assertThat;

class BotConditionTest {

    private static final double CAST_RANGE = 6;

    @Test
    void namesTheAlliedUnitItMatchedSoTheActionCanAimAtIt() {
        ObservedObject seedSpirit = object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0));
        BotWorldView view = view().with(seedSpirit).build();

        Optional<ConditionMatch> match = ally(PrefabType.SeedSpirit).inCastRange().match(context(view));

        assertThat(match).isPresent();
        assertThat(match.get().objects()).containsExactly(seedSpirit);
    }

    @Test
    void doesNotMatchTheSamePrefabOnTheEnemySide() {
        BotWorldView view = view()
                .with(object(1, ENEMY_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0)))
                .build();

        assertThat(ally(PrefabType.SeedSpirit).inCastRange().match(context(view))).isEmpty();
    }

    @Test
    void doesNotMatchAnAlliedUnitOfADifferentPrefab() {
        BotWorldView view = view()
                .with(object(1, BOT_SIDE, PrefabType.RockGolem, SELF.plus(3, 0, 0)))
                .build();

        assertThat(ally(PrefabType.SeedSpirit).inCastRange().match(context(view))).isEmpty();
    }

    @Test
    void treatsCastRangeAsTheDistanceBoundWhenTheTacticSaysInCastRange() {
        BotWorldView view = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(9, 0, 0)))
                .build();

        assertThat(ally(PrefabType.SeedSpirit).inCastRange().match(context(view))).isEmpty();
        assertThat(ally(PrefabType.SeedSpirit).inCastRange().match(new ConditionContext(view, 12)))
                .isPresent();
    }

    @Test
    void ranksMatchesNearestFirst() {
        ObservedObject far = object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(5, 0, 0));
        ObservedObject near = object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(2, 0, 0));
        BotWorldView view = view().with(far, near).build();

        Optional<ConditionMatch> match = enemy().inCastRange().match(context(view));

        assertThat(match).isPresent();
        assertThat(match.get().objects()).containsExactly(near, far);
    }

    @Test
    void skipsObjectsThatCanNoLongerBeHitUnlessAskedNotTo() {
        ObservedObject corpse = object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(2, 0, 0),
                Status.Dying, true, false, Set.of(), Vector3.ZERO);
        BotWorldView view = view().with(corpse).build();

        assertThat(enemy().inCastRange().match(context(view))).isEmpty();
        assertThat(enemy().includeUntargetable().inCastRange().match(context(view))).isPresent();
    }

    @Test
    void matchesOnlyWhatIsSwingingWhenTheTacticSaysAttacking() {
        ObservedObject idle = object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(2, 0, 0));
        ObservedObject swinging = object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(3, 0, 0),
                Status.Attack, true, true, Set.of(), Vector3.ZERO);
        BotWorldView view = view().with(idle, swinging).build();

        Optional<ConditionMatch> match = enemy().attacking().inCastRange().match(context(view));

        assertThat(match).isPresent();
        assertThat(match.get().objects()).containsExactly(swinging);
    }

    @Test
    void matchesOnlyWhatIsComingAtTheBotFastEnough() {
        ObservedObject incoming = object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 0),
                Status.Move, true, true, Set.of(), new Vector3(-2, 0, 0));
        ObservedObject leaving = object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(3, 0, 0),
                Status.Move, true, true, Set.of(), new Vector3(2, 0, 0));
        ObservedObject crossing = object(3, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(5, 0, 0),
                Status.Move, true, true, Set.of(), new Vector3(0, 0, 2));
        BotWorldView view = view().with(incoming, leaving, crossing).build();

        Optional<ConditionMatch> match =
                enemy().approachingAtLeast(1.5).inCastRange().match(context(view));

        assertThat(match).isPresent();
        assertThat(match.get().objects()).containsExactly(incoming);
    }

    @Test
    void matchesOnTagsAndOnMobbishness() {
        ObservedObject taggedMob = object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(2, 0, 0),
                Status.Idle, true, true, Set.of("armored", "ground"), Vector3.ZERO);
        ObservedObject taggedStructure = object(2, ENEMY_SIDE, PrefabType.Overgrowth, SELF.plus(3, 0, 0),
                Status.Idle, false, true, Set.of("armored"), Vector3.ZERO);
        BotWorldView view = view().with(taggedMob, taggedStructure).build();

        assertThat(matched(enemy().withTag("armored").inCastRange(), view))
                .containsExactly(taggedMob, taggedStructure);
        assertThat(matched(enemy().withTag("armored").mobs().inCastRange(), view))
                .containsExactly(taggedMob);
        assertThat(matched(enemy().withTag("armored", "ground").inCastRange(), view))
                .containsExactly(taggedMob);
    }

    @Test
    void requiresAsManySubjectsAsTheTacticAskedFor() {
        BotWorldView view = view()
                .with(object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(2, 0, 0)),
                        object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(3, 0, 0)))
                .build();

        assertThat(enemy().atLeast(2).inCastRange().match(context(view))).isPresent();
        assertThat(enemy().atLeast(3).inCastRange().match(context(view))).isEmpty();
    }

    @Test
    void ignoresDistanceEntirelyWhenTheTacticSaysAnywhere() {
        BotWorldView view = view()
                .with(object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(40, 0, 0)))
                .build();

        assertThat(enemy().anywhere().match(context(view))).isPresent();
        assertThat(enemy().within(5).match(context(view))).isEmpty();
    }

    @Test
    void matchesAClusterAtTheMinimumSizeAndRejectsOneBelowIt() {
        BotWorldView withThree = view().withCluster(mobCluster(3)).build();
        BotWorldView withTwo = view().withCluster(mobCluster(2)).build();

        assertThat(enemyCluster().minSize(3).inCastRange().match(context(withThree))).isPresent();
        assertThat(enemyCluster().minSize(3).inCastRange().match(context(withTwo))).isEmpty();
    }

    @Test
    void rejectsAClusterWhoseCentreIsOutOfCastRange() {
        BotWorldView view = view()
                .withCluster(cluster(SELF.plus(9, 0, 0),
                        object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(9, 0, 0)),
                        object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(9, 0, 1)),
                        object(3, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(9, 0, -1))))
                .build();

        assertThat(enemyCluster().minSize(3).inCastRange().match(context(view))).isEmpty();
        assertThat(enemyCluster().minSize(3).inCastRange().match(new ConditionContext(view, 12)))
                .isPresent();
    }

    @Test
    void ranksClustersBiggestFirst() {
        var small = mobCluster(3);
        var big = cluster(SELF.plus(4, 0, 0),
                object(11, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 0)),
                object(12, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 1)),
                object(13, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, -1)),
                object(14, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4.5f, 0, 0)));
        BotWorldView view = view().withCluster(small, big).build();

        Optional<ConditionMatch> match = enemyCluster().minSize(3).inCastRange().match(context(view));

        assertThat(match).isPresent();
        assertThat(match.get().clusters()).containsExactly(big, small);
    }

    @Test
    void countsOnlyTheClusterMembersTheTacticNamed() {
        var mixed = cluster(SELF.plus(3, 0, 0),
                object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(3, 0, 0)),
                object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(3, 0, 1)),
                object(3, ENEMY_SIDE, PrefabType.EmberSpirit, SELF.plus(3, 0, -1)));
        BotWorldView view = view().withCluster(mixed).build();

        assertThat(enemyCluster().minSize(3).of(PrefabType.RockGolem).inCastRange().match(context(view)))
                .isEmpty();
        assertThat(enemyCluster().minSize(2).of(PrefabType.RockGolem).inCastRange().match(context(view)))
                .isPresent();
    }

    @Test
    void skipsAClusterOfChaffWhenTheTacticAsksForSubstance() {
        BotWorldView view = view().withCluster(mobCluster(3)).build();

        assertThat(enemyCluster().minSize(3).minTotalHp(30).inCastRange().match(context(view))).isPresent();
        assertThat(enemyCluster().minSize(3).minTotalHp(31).inCastRange().match(context(view))).isEmpty();
    }

    @Test
    void readsManaPressureAndHandOffTheBotItself() {
        BotWorldView view = view()
                .mana(6)
                .hand(CardType.Explode, CardType.Fire, CardType.Fire)
                .pressureFrom(1)
                .pressureFrom(2)
                .build();

        assertThat(manaAtLeast(6).match(context(view))).isPresent();
        assertThat(manaAtLeast(7).match(context(view))).isEmpty();
        assertThat(handContains(CardType.Fire, CardType.Fire).match(context(view))).isPresent();
        assertThat(handContains(CardType.Fire, CardType.Fire, CardType.Fire).match(context(view))).isEmpty();
        assertThat(view.pressure()).isGreaterThan(0.4);
        assertThat(pressureAtLeast(0.4).match(context(view))).isPresent();
        assertThat(pressureAtLeast(0.99).match(context(view))).isEmpty();
    }

    @Test
    void carriesBothSubjectsForwardThroughAnAnd() {
        ObservedObject seedSpirit = object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(2, 0, 0));
        BotWorldView view = view().with(seedSpirit).mana(8).build();

        Optional<ConditionMatch> match = ally(PrefabType.SeedSpirit).inCastRange()
                .and(manaAtLeast(5))
                .match(context(view));

        assertThat(match).isPresent();
        assertThat(match.get().objects()).containsExactly(seedSpirit);
    }

    @Test
    void failsTheWholeAndWhenOneClauseFails() {
        BotWorldView view = view()
                .with(object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(2, 0, 0)))
                .mana(3)
                .build();

        assertThat(ally(PrefabType.SeedSpirit).inCastRange().and(manaAtLeast(5)).match(context(view)))
                .isEmpty();
    }

    @Test
    void mergesTheSubjectsOfEveryClauseThatHeldInAnOr() {
        ObservedObject golem = object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(2, 0, 0));
        ObservedObject spirit = object(2, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0));
        BotWorldView view = view().with(golem, spirit).build();

        Optional<ConditionMatch> match = enemy(PrefabType.RockGolem).inCastRange()
                .or(ally(PrefabType.SeedSpirit).inCastRange())
                .match(context(view));

        assertThat(match).isPresent();
        assertThat(match.get().objects()).containsExactly(golem, spirit);
    }

    @Test
    void failsAnOrOnlyWhenEveryClauseFails() {
        BotWorldView view = view().build();

        assertThat(enemy().inCastRange().or(ally().inCastRange()).match(context(view))).isEmpty();
    }

    @Test
    void invertsAClauseAndNamesNoSubject() {
        BotWorldView empty = view().build();
        BotWorldView occupied = view()
                .with(object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(2, 0, 0)))
                .build();

        Optional<ConditionMatch> match = not(enemy().inCastRange()).match(context(empty));

        assertThat(match).isPresent();
        assertThat(match.get().hasSubjects()).isFalse();
        assertThat(not(enemy().inCastRange()).match(context(occupied))).isEmpty();
    }

    @Test
    void looksAtBothSidesWhenTheTacticSaysAnyUnit() {
        ObservedObject ally = object(1, BOT_SIDE, PrefabType.RockGolem, SELF.plus(2, 0, 0));
        ObservedObject foe = object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(3, 0, 0));
        BotWorldView view = view().with(ally, foe).build();

        Optional<ConditionMatch> match = anyUnit(PrefabType.RockGolem).inCastRange().match(context(view));

        assertThat(match).isPresent();
        assertThat(match.get().objects()).containsExactly(ally, foe);
    }

    private static List<ObservedObject> matched(BotCondition condition, BotWorldView view) {
        return condition.match(context(view)).orElseThrow().objects();
    }

    private static ConditionContext context(BotWorldView view) {
        return new ConditionContext(view, CAST_RANGE);
    }

    private static com.wordonline.server.game.domain.bot.view.ObjectCluster mobCluster(int size) {
        ObservedObject[] members = new ObservedObject[size];
        for (int index = 0; index < size; index++) {
            members[index] = object(index + 1, ENEMY_SIDE, PrefabType.RockGolem,
                    SELF.plus(3, 0, index - (size / 2f)));
        }
        return cluster(SELF.plus(3, 0, 0), members);
    }
}
