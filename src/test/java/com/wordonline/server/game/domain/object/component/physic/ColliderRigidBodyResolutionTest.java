package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ColliderRigidBodyResolutionTest {

    private GameObject newGameObject() {
        return new GameObject(Master.LeftPlayer, PrefabType.FireSpirit, Vector3.ZERO, mock(GameContext.class));
    }

    @Test
    void resolvesRigidBodyRegisteredThroughDeferredAddComponentQueue() {
        GameObject gameObject = newGameObject();
        CircleCollider collider = new CircleCollider(gameObject, 0.5f, false);
        gameObject.addCollider(collider);

        // prefab idiom: collider exists before the deferred component queue is flushed
        gameObject.addComponent(new RigidBody(gameObject, 4));
        assertThat(collider.getInvMass()).isZero();

        gameObject.flushComponents();

        assertThat(collider.getInvMass()).isEqualTo(0.25f);
        assertThat(collider.getVelocity()).isSameAs(gameObject.getComponent(RigidBody.class).getVelocity());
    }

    @Test
    void reportsZeroInvMassWhenNoRigidBodyExists() {
        GameObject gameObject = newGameObject();
        CircleCollider collider = new CircleCollider(gameObject, 0.5f, false);

        assertThat(collider.getInvMass()).isZero();
        assertThat(collider.getVelocity()).isEqualTo(Vector3.ZERO);
    }
}
