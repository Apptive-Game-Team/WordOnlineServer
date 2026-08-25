package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CombatRangeTest {

    @Test
    void includesBothColliderRadiiInHorizontalRange() {
        GameObject source = object(new Vector3(0f, 0f, 0f), 1f);
        GameObject touchingBoundary = object(new Vector3(7f, 0f, 0f), 1f);
        GameObject outsideBoundary = object(new Vector3(7.01f, 0f, 0f), 1f);

        assertThat(CombatRange.contains(source, touchingBoundary, 5f)).isTrue();
        assertThat(CombatRange.contains(source, outsideBoundary, 5f)).isFalse();
    }

    @Test
    void usesRangeAsTheMaximumVerticalDifference() {
        GameObject source = object(Vector3.ZERO, 1f);
        GameObject touchingTop = object(new Vector3(7f, 5f, 0f), 1f);
        GameObject aboveTop = object(new Vector3(0f, 5.01f, 0f), 1f);

        assertThat(CombatRange.contains(source, touchingTop, 5f)).isTrue();
        assertThat(CombatRange.contains(source, aboveTop, 5f)).isFalse();
    }

    @Test
    void longerTowerRangeWinsAgainstAnAerialUnitAtTheSameEdgeDistance() {
        GameObject tower = object(Vector3.ZERO, 1f);
        GameObject aerialUnit = object(new Vector3(6f, 3f, 0f), 0.5f);

        assertThat(CombatRange.contains(tower, aerialUnit, 5f)).isTrue();
        assertThat(CombatRange.contains(aerialUnit, tower, 4f)).isFalse();
    }

    private GameObject object(Vector3 position, float radius) {
        GameObject gameObject = mock(GameObject.class);
        CircleCollider collider = mock(CircleCollider.class);
        when(gameObject.getPosition()).thenReturn(position);
        when(gameObject.getFirstCircleCollider(false)).thenReturn(Optional.of(collider));
        when(collider.getRadius()).thenReturn(radius);
        return gameObject;
    }
}
