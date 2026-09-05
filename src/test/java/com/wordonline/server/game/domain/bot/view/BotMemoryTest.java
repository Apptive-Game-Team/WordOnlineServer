package com.wordonline.server.game.domain.bot.view;

import com.wordonline.server.game.domain.bot.BotVisibleObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BotMemoryTest {

    private static final Vector3 BOT_POSITION = new Vector3(1, 0, 5);

    private final BotMemory memory = new BotMemory();

    // A body that has only been seen once has no history to difference against. Guessing anything
    // other than zero would make everything that walks onto the field look like it is charging.
    @Test
    void aFirstSightingStandsStill() {
        memory.observe(List.of(visible(1, new Vector3(5, 0, 5))), 1_000);

        assertThat(memory.velocityOf(1)).isEqualTo(new Vector3(0, 0, 0));
    }

    @Test
    void differencesTwoPassesIntoUnitsPerSecond() {
        memory.observe(List.of(visible(1, new Vector3(5, 0, 5))), 1_000);
        memory.observe(List.of(visible(1, new Vector3(3, 0, 5))), 1_500);

        // Two units to the left in half a second.
        assertThat(memory.velocityOf(1)).isEqualTo(new Vector3(-4, 0, 0));
    }

    @Test
    void anObjectThatWasNeverSeenHasNoVelocity() {
        memory.observe(List.of(visible(1, new Vector3(5, 0, 5))), 1_000);

        assertThat(memory.knows(2)).isFalse();
        assertThat(memory.velocityOf(2)).isEqualTo(new Vector3(0, 0, 0));
    }

    @Test
    void tellsAChargeApartFromARetreat() {
        memory.observe(List.of(
                visible(1, new Vector3(5, 0, 5)),
                visible(2, new Vector3(5, 0, 5))), 1_000);
        memory.observe(List.of(
                visible(1, new Vector3(4, 0, 5)),
                visible(2, new Vector3(6, 0, 5))), 2_000);

        assertThat(observed(1, new Vector3(4, 0, 5)).approachSpeedToward(BOT_POSITION)).isEqualTo(1.0);
        assertThat(observed(2, new Vector3(6, 0, 5)).approachSpeedToward(BOT_POSITION)).isEqualTo(-1.0);
    }

    // Without this the map grows by one entry per body that ever stood on the field, for the whole
    // session.
    @Test
    void forgetsWhatIsNoLongerOnTheField() {
        memory.observe(List.of(
                visible(1, new Vector3(5, 0, 5)),
                visible(2, new Vector3(6, 0, 5))), 1_000);

        memory.observe(List.of(visible(1, new Vector3(5, 0, 5))), 2_000);

        assertThat(memory.trackedCount()).isEqualTo(1);
        assertThat(memory.knows(2)).isFalse();
        assertThat(memory.velocityOf(2)).isEqualTo(new Vector3(0, 0, 0));
    }

    // A body that comes back after being forgotten is a first sighting again, not a teleport from
    // wherever it was last seen.
    @Test
    void anObjectThatComesBackStartsOver() {
        memory.observe(List.of(visible(1, new Vector3(5, 0, 5))), 1_000);
        memory.observe(List.of(), 2_000);
        memory.observe(List.of(visible(1, new Vector3(0, 0, 5))), 3_000);

        assertThat(memory.velocityOf(1)).isEqualTo(new Vector3(0, 0, 0));
    }

    @Test
    void twoPassesAtTheSameInstantDoNotDivideByZero() {
        memory.observe(List.of(visible(1, new Vector3(5, 0, 5))), 1_000);
        memory.observe(List.of(visible(1, new Vector3(3, 0, 5))), 1_000);

        Vector3 velocity = memory.velocityOf(1);
        assertThat(velocity.hasNaN()).isFalse();
        assertThat(velocity).isEqualTo(new Vector3(0, 0, 0));
    }

    @Test
    void aClockThatStepsBackReportsNoMovementRatherThanReversedMovement() {
        memory.observe(List.of(visible(1, new Vector3(5, 0, 5))), 2_000);
        memory.observe(List.of(visible(1, new Vector3(3, 0, 5))), 1_000);

        assertThat(memory.velocityOf(1)).isEqualTo(new Vector3(0, 0, 0));
    }

    // Vector3 is mutable, so a caller that adjusts what it was handed must not be editing the
    // memory itself.
    @Test
    void handsOutCopiesOfWhatItRemembers() {
        memory.observe(List.of(visible(1, new Vector3(5, 0, 5))), 1_000);
        memory.observe(List.of(visible(1, new Vector3(6, 0, 5))), 2_000);

        memory.velocityOf(1).setX(99);

        assertThat(memory.velocityOf(1)).isEqualTo(new Vector3(1, 0, 0));
    }

    private ObservedObject observed(int id, Vector3 position) {
        return new ObservedObject(id, Master.RightPlayer, PrefabType.WaterSlime, position,
                Status.Move, 10, true, true, Set.of(), memory.velocityOf(id));
    }

    private static BotVisibleObject visible(int id, Vector3 position) {
        return new BotVisibleObject(
                id, Master.RightPlayer, PrefabType.WaterSlime, position, Status.Move, 10, true, true);
    }
}
