package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.BotWorldView;
import com.wordonline.server.game.domain.bot.view.ObjectCluster;
import com.wordonline.server.game.domain.bot.view.ObservedObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.BOT_SIDE;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.ENEMY_SIDE;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.SELF;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.cluster;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.object;
import static com.wordonline.server.game.domain.bot.rule.BotRuleTestFixtures.view;
import static com.wordonline.server.game.domain.bot.rule.BotRules.clusterCenter;
import static com.wordonline.server.game.domain.bot.rule.BotRules.forwardOffset;
import static com.wordonline.server.game.domain.bot.rule.BotRules.matchedTarget;
import static com.wordonline.server.game.domain.bot.rule.BotRules.selfPosition;
import static org.assertj.core.api.Assertions.assertThat;

class AimSpecTest {

    @Test
    void aimsOnceAtEveryObjectTheConditionFound() {
        ObservedObject first = object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(2, 0, 0));
        ObservedObject second = object(2, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(3, 0, 0));
        ConditionContext context = context(view().with(first, second).build());

        List<Aim> aims = matchedTarget().resolve(context, ConditionMatch.ofObjects(List.of(first, second)));

        assertThat(aims).hasSize(2);
        assertThat(aims.get(0).position()).isEqualTo(first.position());
        assertThat(aims.get(0).object()).isEqualTo(first);
        assertThat(aims.get(0).description()).isEqualTo("allied SeedSpirit 1");
        assertThat(aims.get(1).description()).isEqualTo("allied SeedSpirit 2");
    }

    @Test
    void namesAnEnemySubjectAsSuch() {
        ObservedObject foe = object(7, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(2, 0, 0));
        ConditionContext context = context(view().with(foe).build());

        List<Aim> aims = matchedTarget().resolve(context, ConditionMatch.ofObjects(List.of(foe)));

        assertThat(aims).singleElement()
                .extracting(Aim::description)
                .isEqualTo("enemy RockGolem 7");
    }

    @Test
    void aimsAtTheMiddleOfEveryGroupTheConditionFound() {
        ObjectCluster group = cluster(SELF.plus(4, 0, 0),
                object(1, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 1)),
                object(2, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, -1)),
                object(3, ENEMY_SIDE, PrefabType.RockGolem, SELF.plus(4, 0, 0)));
        ConditionContext context = context(view().withCluster(group).build());

        List<Aim> aims = clusterCenter().resolve(context, ConditionMatch.ofClusters(List.of(group)));

        assertThat(aims).singleElement().satisfies(aim -> {
            assertThat(aim.position()).isEqualTo(group.center());
            assertThat(aim.cluster()).isEqualTo(group);
            assertThat(aim.description()).isEqualTo("a cluster of 3 enemy mobs");
        });
    }

    @Test
    void aimsAtNothingWhenTheMatchNamedNoSubjectOfThatKind() {
        ConditionContext context = context(view().build());

        assertThat(matchedTarget().resolve(context, ConditionMatch.empty())).isEmpty();
        assertThat(clusterCenter().resolve(context, ConditionMatch.empty())).isEmpty();
    }

    @Test
    void aimsAtTheBotItselfOrAFixedStepTowardsTheEnemy() {
        ConditionContext context = context(view().build());

        assertThat(selfPosition().resolve(context, ConditionMatch.empty()))
                .singleElement()
                .extracting(Aim::position)
                .isEqualTo(SELF);
        assertThat(forwardOffset(3).resolve(context, ConditionMatch.empty()))
                .singleElement()
                .extracting(Aim::position)
                .isEqualTo(new Vector3(SELF.getX() + 3, SELF.getY(), SELF.getZ()));
    }

    @Test
    void doesNotHandOutTheViewsOwnVectorSoACallerCannotMoveIt() {
        ObservedObject subject = object(1, BOT_SIDE, PrefabType.SeedSpirit, SELF.plus(2, 0, 0));
        ConditionContext context = context(view().with(subject).build());

        Aim aim = matchedTarget().resolve(context, ConditionMatch.ofObjects(List.of(subject))).getFirst();
        aim.position().add(new Vector3(100, 0, 0));

        assertThat(subject.position()).isEqualTo(SELF.plus(2, 0, 0));
    }

    private static ConditionContext context(BotWorldView view) {
        return new ConditionContext(view, 8);
    }
}
