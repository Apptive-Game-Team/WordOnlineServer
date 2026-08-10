package com.wordonline.server.game.domain.object.component.build;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.TimedMassPush;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.util.Physics;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WindPushComponentTest {

    @Test
    void appliesPushEveryHalfSecondToEnemiesOnly() {
        GameObject totem = mock(GameObject.class);
        GameObject enemy = pushableMob(Master.RightPlayer);
        GameObject ally = pushableMob(Master.LeftPlayer);
        GameContext gameContext = mock(GameContext.class);
        Physics physics = mock(Physics.class);

        when(totem.getMaster()).thenReturn(Master.LeftPlayer);
        when(totem.getPosition()).thenReturn(Vector3.ZERO);
        when(totem.getGameContext()).thenReturn(gameContext);
        when(gameContext.getPhysics()).thenReturn(physics);
        when(gameContext.getDeltaTime()).thenReturn(0.25f);
        when(physics.overlapBoxAll(any(), any())).thenReturn(List.of(enemy, ally));

        WindPushComponent windPush = new WindPushComponent(totem, 10f, new Vector3(6f, 3f, 1f));
        windPush.update();
        windPush.update();
        windPush.update();

        ArgumentCaptor<Component> componentCaptor = ArgumentCaptor.forClass(Component.class);
        verify(enemy, times(2)).addComponent(componentCaptor.capture());
        assertThat(componentCaptor.getAllValues()).allMatch(TimedMassPush.class::isInstance);
        verify(ally, never()).addComponent(any());
    }

    private GameObject pushableMob(Master master) {
        GameObject target = mock(GameObject.class);
        RigidBody rigidBody = mock(RigidBody.class);
        when(target.getMaster()).thenReturn(master);
        when(target.hasComponent(Mob.class)).thenReturn(true);
        when(target.getComponent(RigidBody.class)).thenReturn(rigidBody);
        when(target.getComponents(TimedMassPush.class)).thenReturn(List.of());
        when(target.getComponentsToAdd()).thenReturn(List.of());
        when(rigidBody.getMass()).thenReturn(1);
        return target;
    }
}
