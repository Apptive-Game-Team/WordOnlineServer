package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ThreatAssessmentTest {

    private static final Vector3 DEFENDED = GameConfig.LEFT_PLAYER_POSITION;

    @Test
    void ignoresOwnObjectsAndUntargetableEnemies() {
        GameObject ally = enemy(Master.LeftPlayer, new Vector3(4, 0, 5), Status.Idle);
        GameObject dying = enemy(Master.RightPlayer, new Vector3(4, 0, 5), Status.Dying);
        GameObject destroyed = enemy(Master.RightPlayer, new Vector3(4, 0, 5), Status.Destroyed);
        GameObject live = enemy(Master.RightPlayer, new Vector3(6, 0, 5), Status.Idle);

        ThreatAssessment threats = ThreatAssessment.observe(
                List.of(ally, dying, destroyed, live), Master.RightPlayer, DEFENDED, 100);

        assertThat(threats.threats()).hasSize(1);
        assertThat(threats.threats().getFirst().objectId()).isEqualTo(live.getId());
    }

    @Test
    void readsRemainingHitPointsFromTheMobComponent() {
        GameObject wounded = enemy(Master.RightPlayer, new Vector3(6, 0, 5), Status.Idle);
        attachMob(wounded, 7);

        ThreatAssessment threats = ThreatAssessment.observe(
                List.of(wounded), Master.RightPlayer, DEFENDED, 100);

        assertThat(threats.threats().getFirst().hp()).isEqualTo(7);
    }

    @Test
    void ordersThreatsByHowCloseTheyAreToTheDefendedPosition() {
        GameObject far = enemy(Master.RightPlayer, new Vector3(12, 0, 5), Status.Idle);
        GameObject near = enemy(Master.RightPlayer, new Vector3(3, 0, 5), Status.Idle);

        ThreatAssessment threats = ThreatAssessment.observe(
                List.of(far, near), Master.RightPlayer, DEFENDED, 100);

        assertThat(threats.threats().getFirst().objectId()).isEqualTo(near.getId());
    }

    @Test
    void pressureRisesAsEnemiesCloseInAndStaysZeroWhenTheyAreFarAway() {
        ThreatAssessment distant = ThreatAssessment.observe(
                List.of(enemy(Master.RightPlayer, new Vector3(15, 0, 5), Status.Idle)),
                Master.RightPlayer, DEFENDED, 100);
        ThreatAssessment atTheDoor = ThreatAssessment.observe(
                List.of(
                        enemy(Master.RightPlayer, new Vector3(2, 0, 5), Status.Idle),
                        enemy(Master.RightPlayer, new Vector3(2, 0, 6), Status.Idle),
                        enemy(Master.RightPlayer, new Vector3(2, 0, 4), Status.Idle)),
                Master.RightPlayer, DEFENDED, 100);

        assertThat(distant.pressure()).isZero();
        assertThat(atTheDoor.pressure()).isBetween(0.8, 1.0);
    }

    @Test
    void pressureSaturatesAtOneSoAnOverwhelmingPushCannotOutweighEverythingElse() {
        List<GameObject> swarm = java.util.stream.IntStream.range(0, 8)
                .mapToObj(index -> enemy(Master.RightPlayer, new Vector3(2, 0, 2 + index * 0.5f), Status.Idle))
                .map(GameObject.class::cast)
                .toList();

        ThreatAssessment threats = ThreatAssessment.observe(swarm, Master.RightPlayer, DEFENDED, 100);

        assertThat(threats.pressure()).isEqualTo(1.0);
    }

    @Test
    void reportsTheLaneTheNearbyPressureSitsIn() {
        ThreatAssessment threats = ThreatAssessment.observe(
                List.of(enemy(Master.RightPlayer, new Vector3(4, 0, 9), Status.Idle)),
                Master.RightPlayer, DEFENDED, 100);

        assertThat(threats.contestedLaneZ()).isEqualTo(9f);
    }

    @Test
    void fallsBackToTheDefendedLaneWhenNothingIsClose() {
        ThreatAssessment threats = ThreatAssessment.observe(List.of(), Master.RightPlayer, DEFENDED, 100);

        assertThat(threats.hasThreats()).isFalse();
        assertThat(threats.contestedLaneZ()).isEqualTo(DEFENDED.getZ());
    }

    @Test
    void marksTheEnemyPlayerAsTheCoreAndTakesItsHpFromTheSession() {
        GameObject core = enemy(Master.RightPlayer, GameConfig.RIGHT_PLAYER_POSITION, Status.Idle, PrefabType.Player);

        ThreatAssessment threats = ThreatAssessment.observe(
                List.of(core), Master.RightPlayer, DEFENDED, 63);

        EnemyThreat observed = threats.threats().getFirst();
        assertThat(observed.playerCore()).isTrue();
        assertThat(observed.hp()).isEqualTo(63);
        assertThat(threats.pressure()).isZero();
    }

    private static GameObject enemy(Master master, Vector3 position, Status status) {
        return enemy(master, position, status, PrefabType.RockSlime);
    }

    private static GameObject enemy(Master master, Vector3 position, Status status, PrefabType type) {
        GameObject gameObject = new GameObject(master, type, position, mock(GameContext.class));
        gameObject.setStatus(status);
        return gameObject;
    }

    private static void attachMob(GameObject gameObject, int hp) {
        gameObject.addComponent(new TestMob(gameObject, hp));
        gameObject.flushComponents();
    }

    private static class TestMob extends Mob {
        private TestMob(GameObject gameObject, int maxHp) {
            super(gameObject, maxHp, 1f);
        }

        @Override
        public void onDeath() {
        }

        @Override
        public void start() {
        }

        @Override
        public void onDestroy() {
        }
    }
}
