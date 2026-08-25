package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.directive.MovementDirective;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.util.Physics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Directive selection used to sort the component list every frame and filter afterwards. The scan
 * that replaced it asks getMoveTarget in a different order, so every case here is also checked
 * against the sort-then-filter it replaced, and the two must agree on the winner.
 */
class MovementDirectiveSelectionTest {

    private GameContext gameContext;
    private GameObject mobObject;
    private BehaviorMob mob;

    @BeforeEach
    void setUp() {
        gameContext = mock(GameContext.class);
        GameSessionData sessionData = new GameSessionData(mock(PlayerData.class), mock(PlayerData.class));
        when(gameContext.getGameSessionData()).thenReturn(sessionData);
        when(gameContext.getDeltaTime()).thenReturn(0.05f);
        Physics physics = mock(Physics.class);
        when(gameContext.getPhysics()).thenReturn(physics);
        when(physics.overlapSphereAll(any(GameObject.class), anyFloat())).thenReturn(List.of());

        mobObject = new GameObject(Master.LeftPlayer, PrefabType.RockSlime, Vector3.ZERO, gameContext);
        mobObject.setStatus(Status.Idle);
        mobObject.addCollider(new CircleCollider(mobObject, 0.5f, false));
        mobObject.getComponents().add(new RigidBody(mobObject, 1));
        mob = new BehaviorMob(mobObject, 10, 1f, TargetMask.ANY.bit, 0f, 1f, target -> true);
        mobObject.getComponents().add(mob);
    }

    @Test
    void noDirectivesMeansNoDirective() {
        assertThat(mob.resolveHighestPriorityDirective()).isNull();
        assertThat(referenceResolve()).isEmpty();
    }

    @Test
    void theOnlyDirectiveWithATargetWins() {
        TestDirective only = addDirective(10, new Vector3(1f, 0f, 0f));

        assertSelects(only);
    }

    @Test
    void aTiedPriorityGoesToTheDirectiveAddedFirst() {
        TestDirective first = addDirective(100, new Vector3(1f, 0f, 0f));
        addDirective(100, new Vector3(2f, 0f, 0f));

        assertSelects(first);
    }

    @Test
    void aHigherPriorityAddedLaterStillWins() {
        addDirective(10, new Vector3(1f, 0f, 0f));
        TestDirective stronger = addDirective(100, new Vector3(2f, 0f, 0f));
        addDirective(50, new Vector3(3f, 0f, 0f));

        assertSelects(stronger);
    }

    @Test
    void aHighestPriorityDirectiveWithNoTargetIsSkipped() {
        addDirective(100, null);
        TestDirective usable = addDirective(50, new Vector3(1f, 0f, 0f));

        assertSelects(usable);
    }

    @Test
    void aTieIsBrokenByTheFirstDirectiveThatActuallyHasATarget() {
        addDirective(100, null);
        TestDirective usable = addDirective(100, new Vector3(1f, 0f, 0f));
        addDirective(100, new Vector3(2f, 0f, 0f));

        assertSelects(usable);
    }

    @Test
    void noDirectiveWithATargetMeansNoDirective() {
        addDirective(100, null);
        addDirective(50, null);

        assertThat(mob.resolveHighestPriorityDirective()).isNull();
        assertThat(referenceResolve()).isEmpty();
    }

    @Test
    void selectionOnlyReadsTheDirectivesAndLeavesThemUnchanged() {
        TestDirective stale = addDirective(100, null);
        TestDirective usable = addDirective(50, new Vector3(1f, 0f, 0f));

        mob.resolveHighestPriorityDirective();
        mob.resolveHighestPriorityDirective();

        // getMoveTarget is a pure read on every implementation, which is what lets the scan call it
        // in list order instead of priority order.
        assertThat(stale.moveTarget).isNull();
        assertThat(usable.moveTarget).isEqualTo(new Vector3(1f, 0f, 0f));
        assertThat(mobObject.getComponents()).hasSize(4);
    }

    private void assertSelects(TestDirective expected) {
        assertThat(mob.resolveHighestPriorityDirective()).isSameAs(expected);
        assertThat(referenceResolve()).containsSame(expected);
    }

    /** The implementation this replaced, kept as the oracle for the selection rule. */
    private Optional<MovementDirective> referenceResolve() {
        return mobObject.getComponents(MovementDirective.class).stream()
                .sorted(Comparator.comparingInt(MovementDirective::priority).reversed())
                .filter(directive -> directive.getMoveTarget(mobObject).isPresent())
                .findFirst();
    }

    private TestDirective addDirective(int priority, Vector3 moveTarget) {
        TestDirective directive = new TestDirective(mobObject, priority, moveTarget);
        mobObject.getComponents().add(directive);
        return directive;
    }

    private static class TestDirective extends Component implements MovementDirective {
        private final int priority;
        private final Vector3 moveTarget;

        private TestDirective(GameObject gameObject, int priority, Vector3 moveTarget) {
            super(gameObject);
            this.priority = priority;
            this.moveTarget = moveTarget;
        }

        @Override
        public int priority() {
            return priority;
        }

        @Override
        public Optional<Vector3> getMoveTarget(GameObject self) {
            return Optional.ofNullable(moveTarget);
        }

        @Override
        public float getArrivalDistance() {
            return 0.5f;
        }

        @Override
        public boolean suppressCombat() {
            return true;
        }

        @Override
        public void start() {
        }

        @Override
        public void update() {
        }

        @Override
        public void onDestroy() {
        }
    }
}
