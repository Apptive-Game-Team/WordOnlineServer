package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.Element;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.effect.receiver.EffectReceiver;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RockGolemMobTest {

    @Test
    void meleeAttackDamagesAndLightlyKnocksTargetAway() {
        GameObject rockGolem = mock(GameObject.class);
        GameObject target = mock(GameObject.class);
        GameContext gameContext = mock(GameContext.class);
        Damageable damageable = mock(Damageable.class);
        EffectReceiver effectReceiver = mock(EffectReceiver.class);
        Element rock = new Element();
        rock.addNative(ElementType.ROCK);

        when(rockGolem.getGameContext()).thenReturn(gameContext);
        when(rockGolem.getMaster()).thenReturn(Master.LeftPlayer);
        when(rockGolem.getPosition()).thenReturn(new Vector3(1f, 0f, 1f));
        when(rockGolem.getElement()).thenReturn(rock);
        when(target.getMaster()).thenReturn(Master.RightPlayer);
        when(target.getPosition()).thenReturn(new Vector3(1.18f, 2f, 1.24f));
        when(target.getComponent(Damageable.class)).thenReturn(damageable);
        when(target.getComponent(EffectReceiver.class)).thenReturn(effectReceiver);
        when(gameContext.getDeltaTime()).thenReturn(1.1f);

        RockGolemMob mob = new RockGolemMob(rockGolem, 20, 1f, 1, 7, 1f);
        mob.target = target;
        mob.setState(mob.new AttackState());

        mob.update();

        verify(damageable).onDamaged(new AttackInfo(7, ElementType.ROCK));
        ArgumentCaptor<Vector3> directionCaptor = ArgumentCaptor.forClass(Vector3.class);
        verify(effectReceiver).onReceive(eq(Effect.Knockback), directionCaptor.capture(), eq(0.25f));
        Vector3 direction = directionCaptor.getValue();
        assertThat(direction.getX()).isCloseTo(0.6f, within(0.0001f));
        assertThat(direction.getY()).isZero();
        assertThat(direction.getZ()).isCloseTo(0.8f, within(0.0001f));
        verify(rockGolem).setStatus(Status.Attack);
    }
}
