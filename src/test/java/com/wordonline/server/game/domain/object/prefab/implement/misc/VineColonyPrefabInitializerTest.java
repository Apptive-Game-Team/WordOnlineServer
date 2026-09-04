package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.mob.simple.SummonMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.GameObjectParameters;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
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

class VineColonyPrefabInitializerTest {

    @Test
    void usesRockTurretHitPointsForLifetime() {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters vineColonyParameters = mock(GameObjectParameters.class);
        GameObjectParameters rockTurretParameters = mock(GameObjectParameters.class);
        when(parameters.object(GameObjectKey.VINE_COLONY)).thenReturn(vineColonyParameters);
        when(parameters.object(GameObjectKey.ROCK_TURRET)).thenReturn(rockTurretParameters);
        when(vineColonyParameters.intValue(ParameterKey.MASS)).thenReturn(3);
        when(vineColonyParameters.floatValue(ParameterKey.RADIUS)).thenReturn(1f);
        when(vineColonyParameters.intValue(ParameterKey.HP)).thenReturn(40);
        when(vineColonyParameters.intValue(ParameterKey.DAMAGE)).thenReturn(9);
        when(vineColonyParameters.intValue(ParameterKey.ATTACK_INTERVAL)).thenReturn(1);
        when(vineColonyParameters.intValue(ParameterKey.ATTACK_RANGE)).thenReturn(3);
        when(vineColonyParameters.floatValue(ParameterKey.DURATION)).thenReturn(60f);
        when(rockTurretParameters.intValue(ParameterKey.HP)).thenReturn(15);

        GameObject vineColony = new GameObject(
                Master.LeftPlayer,
                PrefabType.VineColony,
                Vector3.ZERO,
                mock(GameContext.class)
        );

        new VineColonyPrefabInitializer(parameters).initialize(vineColony);

        TimedSelfDestroyer selfDestroyer = components(vineColony)
                .filter(TimedSelfDestroyer.class::isInstance)
                .map(TimedSelfDestroyer.class::cast)
                .findFirst()
                .orElseThrow();
        GaugeDto gauge = selfDestroyer.getGauge();

        assertThat(gauge.maxValue()).isEqualTo(15f);
        assertThat(gauge.maxValue()).isNotEqualTo(60f);
        assertThat(gauge.category()).isEqualTo(GaugeCategory.TTL);
        assertThat(vineColony.getElement().nativeHas(ElementType.NATURE)).isTrue();
        assertThat(components(vineColony)).anyMatch(RigidBody.class::isInstance);
        assertThat(components(vineColony)).anyMatch(SummonMob.class::isInstance);
        assertThat(vineColony.getFirstCircleCollider()).map(CircleCollider::getRadius).hasValue(1f);
    }

    private Stream<Component> components(GameObject gameObject) {
        List<Component> all = Stream.concat(
                gameObject.getComponents().stream(),
                gameObject.getComponentsToAdd().stream()
        ).toList();
        return all.stream();
    }
}
