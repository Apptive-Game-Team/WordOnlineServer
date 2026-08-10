package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TimedMassPushTest {

    @Test
    void scalesPushSpeedByInverseSquareRootOfMass() {
        GameObject source = mock(GameObject.class);
        GameObject target = mock(GameObject.class);
        GameContext gameContext = mock(GameContext.class);
        RigidBody rigidBody = mock(RigidBody.class);

        when(target.getGameContext()).thenReturn(gameContext);
        when(target.getComponent(RigidBody.class)).thenReturn(rigidBody);
        when(rigidBody.getMass()).thenReturn(10);
        when(gameContext.getDeltaTime()).thenReturn(0.1f);

        TimedMassPush.apply(target, source, Vector3.RIGHT, 3f, 0.5f);

        ArgumentCaptor<com.wordonline.server.game.domain.object.component.Component> componentCaptor =
                ArgumentCaptor.forClass(com.wordonline.server.game.domain.object.component.Component.class);
        verify(target).addComponent(componentCaptor.capture());

        TimedMassPush push = (TimedMassPush) componentCaptor.getValue();
        push.update();

        ArgumentCaptor<Vector3> velocityCaptor = ArgumentCaptor.forClass(Vector3.class);
        verify(rigidBody).addVelocity(velocityCaptor.capture());
        assertThat(velocityCaptor.getValue().getX())
                .isCloseTo(3f / (float) Math.sqrt(10f), within(0.0001f));
    }

    @Test
    void immovableMassRejectsPush() {
        GameObject source = mock(GameObject.class);
        GameObject target = mock(GameObject.class);
        RigidBody rigidBody = mock(RigidBody.class);
        when(target.getComponent(RigidBody.class)).thenReturn(rigidBody);
        when(rigidBody.getMass()).thenReturn(-1);

        TimedMassPush.apply(target, source, Vector3.RIGHT, 3f, 0.5f);

        verify(target, org.mockito.Mockito.never())
                .addComponent(org.mockito.ArgumentMatchers.any());
    }
}
