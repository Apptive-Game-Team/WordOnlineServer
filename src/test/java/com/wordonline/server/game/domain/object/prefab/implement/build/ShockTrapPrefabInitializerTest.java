package com.wordonline.server.game.domain.object.prefab.implement.build;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.BuildingEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.ShockTrapDetector;
import com.wordonline.server.game.domain.object.component.mob.simple.DummyMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.GameObjectParameters;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.frame.GaugeCategory;
import com.wordonline.server.game.dto.frame.GaugeDto;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShockTrapPrefabInitializerTest {

    @Test
    void wiresShockTrapComponentsFromParameters() {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters shockTrapParameters = mock(GameObjectParameters.class);
        when(parameters.object(GameObjectKey.SHOCK_TRAP)).thenReturn(shockTrapParameters);
        when(shockTrapParameters.intValue(ParameterKey.MASS)).thenReturn(5);
        when(shockTrapParameters.floatValue(ParameterKey.RADIUS)).thenReturn(2.5f);
        when(shockTrapParameters.intValue(ParameterKey.HP)).thenReturn(30);
        when(shockTrapParameters.floatValue(ParameterKey.TRIGGER_DELAY)).thenReturn(1.2f);
        when(shockTrapParameters.floatValue(ParameterKey.STUN_DURATION)).thenReturn(2.4f);
        when(shockTrapParameters.floatValue(ParameterKey.ATTACK_INTERVAL)).thenReturn(6f);
        when(shockTrapParameters.floatValue(ParameterKey.DURATION)).thenReturn(45f);

        GameObject shockTrap = new GameObject(
                Master.LeftPlayer,
                PrefabType.ShockTrap,
                Vector3.ZERO,
                mock(GameContext.class)
        );

        new ShockTrapPrefabInitializer(parameters).initialize(shockTrap);

        TimedSelfDestroyer selfDestroyer = components(shockTrap)
                .filter(TimedSelfDestroyer.class::isInstance)
                .map(TimedSelfDestroyer.class::cast)
                .findFirst()
                .orElseThrow();
        GaugeDto gauge = selfDestroyer.getGauge();

        assertThat(gauge.maxValue()).isEqualTo(45f);
        assertThat(gauge.category()).isEqualTo(GaugeCategory.TTL);
        assertThat(shockTrap.getElement().nativeHas(ElementType.LIGHTNING)).isTrue();
        assertThat(components(shockTrap)).anyMatch(RigidBody.class::isInstance);
        assertThat(components(shockTrap)).anyMatch(DummyMob.class::isInstance);
        assertThat(components(shockTrap)).anyMatch(BuildingEffectReceiver.class::isInstance);
        assertThat(components(shockTrap)).anyMatch(ShockTrapDetector.class::isInstance);
        assertThat(shockTrap.getFirstCircleCollider()).map(CircleCollider::getRadius).hasValue(2.5f);
    }

    private Stream<Component> components(GameObject gameObject) {
        List<Component> all = Stream.concat(
                gameObject.getComponents().stream(),
                gameObject.getComponentsToAdd().stream()
        ).toList();
        return all.stream();
    }
}
