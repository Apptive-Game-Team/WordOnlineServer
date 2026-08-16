package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.object.Vector3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BlastTargetSelectorTest {

    private static final Vector3 CAST_ORIGIN = new Vector3(1, 0, 5);

    @Test
    void prefersTheClusterOverTheNearestSingleEnemy() {
        EnemyThreat lone = threat(1, 4, 5, 20);
        EnemyThreat clusterA = threat(2, 8, 5, 20);
        EnemyThreat clusterB = threat(3, 8.5f, 5, 20);
        EnemyThreat clusterC = threat(4, 9.9f, 5, 20);

        Optional<BlastTargetSelector.Impact> impact = BlastTargetSelector.bestImpact(
                List.of(lone, clusterA, clusterB, clusterC), CAST_ORIGIN, 18, 1.5, 5);

        assertThat(impact).isPresent();
        assertThat(impact.get().coveredCount()).isEqualTo(3);
        assertThat(impact.get().center().getX()).isEqualTo(8.5f);
    }

    @Test
    void capsConvertedDamageAtRemainingHitPointsSoOverkillDoesNotWin() {
        EnemyThreat almostDead = threat(1, 4, 5, 2);
        EnemyThreat healthy = threat(2, 12, 5, 40);

        Optional<BlastTargetSelector.Impact> impact = BlastTargetSelector.bestImpact(
                List.of(almostDead, healthy), CAST_ORIGIN, 18, 0.5, 20);

        assertThat(impact).isPresent();
        assertThat(impact.get().center().getX()).isEqualTo(12f);
        assertThat(impact.get().expectedDamage()).isEqualTo(20);
    }

    @Test
    void weightsDamageOnTheEnemyPlayerAboveTheSameDamageOnASummon() {
        EnemyThreat summon = threat(1, 4, 5, 100);
        EnemyThreat playerCore = new EnemyThreat(2, new Vector3(17, 0, 5), 100, 16, true);

        Optional<BlastTargetSelector.Impact> impact = BlastTargetSelector.bestImpact(
                List.of(summon, playerCore), CAST_ORIGIN, 18, 0.5, 15);

        assertThat(impact).isPresent();
        assertThat(impact.get().center().getX()).isEqualTo(17f);
        assertThat(impact.get().expectedDamage())
                .isEqualTo(15 * BlastTargetSelector.PLAYER_CORE_WEIGHT);
    }

    @Test
    void reportsNoImpactWhenEveryEnemyIsOutOfCastRange() {
        Optional<BlastTargetSelector.Impact> impact = BlastTargetSelector.bestImpact(
                List.of(threat(1, 15, 5, 20)), CAST_ORIGIN, 9, 1.5, 5);

        assertThat(impact).isEmpty();
    }

    @Test
    void countsCoveredEnemiesTheBlastWouldFinishOff() {
        Optional<BlastTargetSelector.Impact> impact = BlastTargetSelector.bestImpact(
                List.of(threat(1, 6, 5, 2), threat(2, 6.5f, 5, 30)), CAST_ORIGIN, 18, 1.5, 5);

        assertThat(impact).isPresent();
        assertThat(impact.get().coveredCount()).isEqualTo(2);
        assertThat(impact.get().lethalCount()).isEqualTo(1);
    }

    private static EnemyThreat threat(int id, float x, float z, int hp) {
        Vector3 position = new Vector3(x, 0, z);
        return new EnemyThreat(id, position, hp, position.distance(CAST_ORIGIN), false);
    }
}
