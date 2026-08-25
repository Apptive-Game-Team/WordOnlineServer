package com.wordonline.server.game.domain.bot.view;

import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.bot.BotEye;
import com.wordonline.server.game.domain.bot.BotVisibleObject;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BotWorldViewFactoryTest {

    private static final long FIRST_PASS = 1_000;
    private static final long SECOND_PASS = 2_000;

    // The tag seam is a function, so the test states the tag table instead of stubbing a service.
    private final GameObjectTags tags = prefabType ->
            prefabType == PrefabType.WaterSlime ? Set.of("beast") : Set.of();
    private final BotWorldViewFactory factory = new BotWorldViewFactory(tags);
    private final BotMemory memory = new BotMemory();

    @Test
    void splitsTheFieldBySideAndDropsWhatCannotBeHit() {
        BotEye eye = eye(
                object(1, Master.LeftPlayer, PrefabType.FireSpirit, new Vector3(2, 0, 5), true, true),
                object(2, Master.LeftPlayer, PrefabType.FireSpirit, new Vector3(3, 0, 5), true, false),
                object(3, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(9, 0, 5), true, true),
                object(4, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(9, 0, 6), true, false),
                object(5, Master.None, PrefabType.FireField, new Vector3(5, 0, 5), false, true));

        BotWorldView view = factory.build(eye, Master.LeftPlayer, BotTier.ELITE, memory, FIRST_PASS);

        assertThat(view.objects()).extracting(ObservedObject::id).containsExactly(1, 2, 3, 4, 5);
        assertThat(view.allies()).extracting(ObservedObject::id).containsExactly(1);
        assertThat(view.enemies()).extracting(ObservedObject::id).containsExactly(3);
        assertThat(view.hasEnemies()).isTrue();
    }

    @Test
    void readsTheSidesFromThePerspectiveOfTheActingBot() {
        BotEye eye = eye(
                object(1, Master.LeftPlayer, PrefabType.FireSpirit, new Vector3(2, 0, 5), true, true),
                object(2, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(9, 0, 5), true, true));

        BotWorldView view = factory.build(eye, Master.RightPlayer, BotTier.INTRO, memory, FIRST_PASS);

        assertThat(view.botSide()).isEqualTo(Master.RightPlayer);
        assertThat(view.enemySide()).isEqualTo(Master.LeftPlayer);
        assertThat(view.tier()).isEqualTo(BotTier.INTRO);
        assertThat(view.selfPosition()).isEqualTo(GameConfig.RIGHT_PLAYER_POSITION);
        assertThat(view.allies()).extracting(ObservedObject::id).containsExactly(2);
        assertThat(view.enemies()).extracting(ObservedObject::id).containsExactly(1);
    }

    @Test
    void clustersOnlyEnemyMobsThatCanStillBeHit() {
        BotEye eye = eye(
                object(1, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(5, 0, 4), true, true),
                object(2, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(5, 0, 5), true, true),
                object(3, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(5, 0, 6), true, true),
                // A field sitting on top of them is not a body, and a dying mob is already gone.
                object(4, Master.RightPlayer, PrefabType.WaterField, new Vector3(5, 0, 5), false, true),
                object(5, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(5, 0, 5), true, false),
                // The bot's own crowd is not something to drop an explosion on.
                object(6, Master.LeftPlayer, PrefabType.FireSpirit, new Vector3(2, 0, 4), true, true),
                object(7, Master.LeftPlayer, PrefabType.FireSpirit, new Vector3(2, 0, 5), true, true),
                object(8, Master.LeftPlayer, PrefabType.FireSpirit, new Vector3(2, 0, 6), true, true));

        BotWorldView view = factory.build(eye, Master.LeftPlayer, BotTier.ELITE, memory, FIRST_PASS);

        assertThat(view.enemyClusters()).hasSize(1);
        ObjectCluster cluster = view.enemyClusters().getFirst();
        assertThat(cluster.center()).isEqualTo(new Vector3(5, 0, 5));
        assertThat(cluster.members()).extracting(ObservedObject::id).containsExactly(1, 2, 3);
    }

    @Test
    void aThinFieldHasNoClusters() {
        BotEye eye = eye(
                object(1, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(5, 0, 4), true, true),
                object(2, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(15, 0, 5), true, true));

        BotWorldView view = factory.build(eye, Master.LeftPlayer, BotTier.ELITE, memory, FIRST_PASS);

        assertThat(view.enemyClusters()).isEmpty();
    }

    @Test
    void carriesTheTagsAndTheStatusOfEachObject() {
        BotEye eye = eye(object(1, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(9, 0, 5), true, true,
                Status.Attack));

        BotWorldView view = factory.build(eye, Master.LeftPlayer, BotTier.ELITE, memory, FIRST_PASS);

        ObservedObject enemy = view.enemies().getFirst();
        assertThat(enemy.tags()).containsExactly("beast");
        assertThat(enemy.hasTag("beast")).isTrue();
        assertThat(enemy.isAttacking()).isTrue();
    }

    // The second pass is where the velocity comes from, so the view has to be built through the
    // memory rather than reading a live rigid body that has already been cleared.
    @Test
    void givesObjectsTheVelocityRememberedFromTheLastPass() {
        Vector3 defended = GameConfig.LEFT_PLAYER_POSITION;
        factory.build(eye(object(1, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(9, 0, 5), true, true)),
                Master.LeftPlayer, BotTier.ELITE, memory, FIRST_PASS);

        BotWorldView view = factory.build(
                eye(object(1, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(7, 0, 5), true, true)),
                Master.LeftPlayer, BotTier.ELITE, memory, SECOND_PASS);

        ObservedObject enemy = view.enemies().getFirst();
        assertThat(enemy.velocity()).isEqualTo(new Vector3(-2, 0, 0));
        assertThat(enemy.approachSpeedToward(defended)).isEqualTo(2.0);
    }

    @Test
    void reusesTheExistingThreatModelRatherThanRecomputingPressure() {
        BotEye eye = eye(
                object(1, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(2, 0, 5), true, true),
                object(2, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(9, 0, 5), true, true),
                object(3, Master.RightPlayer, PrefabType.WaterSlime, new Vector3(2, 0, 5), true, false));

        BotWorldView view = factory.build(eye, Master.LeftPlayer, BotTier.ELITE, memory, FIRST_PASS);

        assertThat(view.threats().threats()).extracting(threat -> threat.objectId()).containsExactly(1, 2);
        assertThat(view.pressure()).isEqualTo(view.threats().pressure());
        assertThat(view.pressure()).isGreaterThan(0);
    }

    @Test
    void carriesTheHandTheManaAndTheEnemyHitPoints() {
        BotEye eye = new BotEye(List.of(), List.of(CardType.Water, CardType.Explode), 7, 42);

        BotWorldView view = factory.build(eye, Master.LeftPlayer, BotTier.ELITE, memory, FIRST_PASS);

        assertThat(view.mana()).isEqualTo(7);
        assertThat(view.hand()).containsExactly(CardType.Water, CardType.Explode);
        assertThat(view.enemyPlayerHp()).isEqualTo(42);
        assertThat(view.hasEnemies()).isFalse();
    }

    // The view is read many times while rules are matched; nothing a rule does may edit the world
    // the next rule sees, and the shared GameConfig position constant is not the view's to move.
    @Test
    void doesNotHandOutTheSharedPlayerPositionConstant() {
        BotWorldView view = factory.build(
                new BotEye(List.of(), List.of(), 0, 100), Master.LeftPlayer, BotTier.ELITE, memory, FIRST_PASS);

        view.selfPosition().setX(99);

        assertThat(GameConfig.LEFT_PLAYER_POSITION).isEqualTo(new Vector3(1, 0, 5));
    }

    private static BotEye eye(BotVisibleObject... objects) {
        return new BotEye(List.of(objects), List.of(CardType.Water, CardType.Explode), 10, 100);
    }

    private static BotVisibleObject object(int id,
                                           Master master,
                                           PrefabType type,
                                           Vector3 position,
                                           boolean mob,
                                           boolean targetable) {
        return object(id, master, type, position, mob, targetable, Status.Idle);
    }

    private static BotVisibleObject object(int id,
                                           Master master,
                                           PrefabType type,
                                           Vector3 position,
                                           boolean mob,
                                           boolean targetable,
                                           Status status) {
        return new BotVisibleObject(id, master, type, position, status, 10, mob, targetable);
    }
}
