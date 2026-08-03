package com.wordonline.server.game.domain.object.component.mob.directive;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Status;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RallyMoveDirectiveTest {

    @Test
    void allowsCombatOnlyForTargetsInsideTheRallyTotemRange() {
        GameObject ralliedMob = mock(GameObject.class);
        GameObject rallyingTotem = mock(GameObject.class);
        GameObject insideTarget = mock(GameObject.class);
        GameObject outsideTarget = mock(GameObject.class);

        when(rallyingTotem.getStatus()).thenReturn(Status.Idle);
        when(rallyingTotem.getPosition()).thenReturn(Vector3.ZERO);
        when(insideTarget.getPosition()).thenReturn(new Vector3(2f, 0f, 0f));
        when(outsideTarget.getPosition()).thenReturn(new Vector3(2.01f, 0f, 0f));

        RallyMoveDirective directive = new RallyMoveDirective(ralliedMob, rallyingTotem, 2f);

        assertThat(directive.allowsCombatTarget(ralliedMob, insideTarget)).isTrue();
        assertThat(directive.allowsCombatTarget(ralliedMob, outsideTarget)).isFalse();
    }

    @Test
    void disallowsCombatWhenTheRallyTotemIsDestroyed() {
        GameObject ralliedMob = mock(GameObject.class);
        GameObject rallyingTotem = mock(GameObject.class);
        GameObject target = mock(GameObject.class);

        when(rallyingTotem.getStatus()).thenReturn(Status.Destroyed);
        when(rallyingTotem.getPosition()).thenReturn(Vector3.ZERO);
        when(target.getPosition()).thenReturn(Vector3.ZERO);

        RallyMoveDirective directive = new RallyMoveDirective(ralliedMob, rallyingTotem, 2f);

        assertThat(directive.allowsCombatTarget(ralliedMob, target)).isFalse();
    }
}
