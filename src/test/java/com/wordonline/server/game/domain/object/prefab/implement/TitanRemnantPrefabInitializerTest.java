package com.wordonline.server.game.domain.object.prefab.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.OnStartAttacker;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.mob.simple.SummonMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.prefab.implement.misc.TitanRemnantPrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.implement.subprefab.TitanFistPrefabInitializer;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.GameObjectParameters;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.frame.GaugeCategory;
import com.wordonline.server.game.service.GameContext;

class TitanRemnantPrefabInitializerTest {

    @Test
    void remnantUsesRockBuildingComponentsAndLifetime() {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters values = mock(GameObjectParameters.class);
        when(parameters.object(GameObjectKey.TITAN_REMNANT)).thenReturn(values);
        when(values.intValue(ParameterKey.MASS)).thenReturn(8);
        when(values.floatValue(ParameterKey.RADIUS)).thenReturn(1.5f);
        when(values.intValue(ParameterKey.HP)).thenReturn(120);
        when(values.floatValue(ParameterKey.ATTACK_INTERVAL)).thenReturn(2f);
        when(values.floatValue(ParameterKey.ATTACK_RANGE)).thenReturn(5f);
        when(values.floatValue(ParameterKey.DURATION)).thenReturn(15f);

        GameObject remnant = gameObject(PrefabType.TitanRemnant);
        new TitanRemnantPrefabInitializer(parameters).initialize(remnant);

        assertThat(remnant.getElement().nativeHas(ElementType.ROCK)).isTrue();
        assertThat(components(remnant)).anyMatch(RigidBody.class::isInstance);
        assertThat(components(remnant)).anyMatch(SummonMob.class::isInstance);
        assertThat(remnant.getFirstCircleCollider()).map(CircleCollider::getRadius).hasValue(1.5f);

        TimedSelfDestroyer lifetime = components(remnant)
                .filter(TimedSelfDestroyer.class::isInstance)
                .map(TimedSelfDestroyer.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(lifetime.getGauge().maxValue()).isEqualTo(15f);
        assertThat(lifetime.getGauge().category()).isEqualTo(GaugeCategory.TTL);
    }

    @Test
    void fistUsesRockAreaAttackAndShortLifetime() {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters values = mock(GameObjectParameters.class);
        when(parameters.object(GameObjectKey.TITAN_FIST)).thenReturn(values);
        when(values.floatValue(ParameterKey.RADIUS)).thenReturn(2f);
        when(values.intValue(ParameterKey.DAMAGE)).thenReturn(30);
        when(values.floatValue(ParameterKey.DURATION)).thenReturn(1f);

        GameObject fist = gameObject(PrefabType.TitanFist);
        new TitanFistPrefabInitializer(parameters).initialize(fist);

        assertThat(fist.getElement().nativeHas(ElementType.ROCK)).isTrue();
        assertThat(components(fist)).anyMatch(OnStartAttacker.class::isInstance);
        assertThat(components(fist)).anyMatch(TimedSelfDestroyer.class::isInstance);
        assertThat(fist.getFirstCircleCollider()).hasValueSatisfying(collider -> {
            assertThat(collider.getRadius()).isEqualTo(2f);
            assertThat(collider.isTrigger()).isTrue();
        });
    }

    private GameObject gameObject(PrefabType type) {
        return new GameObject(Master.LeftPlayer, type, Vector3.ZERO, mock(GameContext.class));
    }

    private Stream<Component> components(GameObject gameObject) {
        List<Component> all = Stream.concat(
                gameObject.getComponents().stream(),
                gameObject.getComponentsToAdd().stream()
        ).toList();
        return all.stream();
    }
}
