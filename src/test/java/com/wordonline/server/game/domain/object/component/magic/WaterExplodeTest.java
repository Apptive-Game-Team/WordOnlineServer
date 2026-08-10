package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.Element;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WaterExplodeTest {

    @Test
    void scalesVerticalImpulseByInverseSquareRootOfMass() {
        GameObject explosion = mock(GameObject.class);
        GameObject target = mock(GameObject.class);
        RigidBody rigidBody = mock(RigidBody.class);
        ZPhysics zPhysics = mock(ZPhysics.class);
        when(explosion.getElement()).thenReturn(new Element());
        when(target.getComponent(RigidBody.class)).thenReturn(rigidBody);
        when(target.getComponentOptional(ZPhysics.class)).thenReturn(Optional.of(zPhysics));
        when(rigidBody.getMass()).thenReturn(10);

        new WaterExplode(explosion, 3, 1.5f, 10f).handleGameObject(target);

        verify(zPhysics).addImpulseZ(org.mockito.ArgumentMatchers.floatThat(value ->
                Math.abs(value - 10f / (float) Math.sqrt(10f)) < 0.0001f));
    }
}
