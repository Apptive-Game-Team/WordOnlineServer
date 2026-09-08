package com.wordonline.server.game.domain.object.prefab.implement.build;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.BuildingEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.GrassSpread;
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

class GrassGeneratorPrefabInitializerTest {

    @Test
    void wiresConfiguredGrassGeneratorParametersIntoItsComponents() {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters grassGeneratorParameters = mock(GameObjectParameters.class);
        when(parameters.object(GameObjectKey.GRASS_GENERATOR)).thenReturn(grassGeneratorParameters);
        when(grassGeneratorParameters.intValue(ParameterKey.MASS)).thenReturn(99999);
        when(grassGeneratorParameters.floatValue(ParameterKey.RADIUS)).thenReturn(5f);
        when(grassGeneratorParameters.intValue(ParameterKey.HP)).thenReturn(60);
        when(grassGeneratorParameters.floatValue(ParameterKey.ATTACK_INTERVAL)).thenReturn(3f);
        when(grassGeneratorParameters.intValue(ParameterKey.QUANTITY)).thenReturn(6);
        when(grassGeneratorParameters.floatValue(ParameterKey.DURATION)).thenReturn(20f);

        GameObject grassGenerator = new GameObject(
                Master.LeftPlayer,
                PrefabType.GrassGenerator,
                Vector3.ZERO,
                mock(GameContext.class)
        );

        new GrassGeneratorPrefabInitializer(parameters).initialize(grassGenerator);

        List<Component> components = components(grassGenerator).toList();

        assertThat(components).anyMatch(RigidBody.class::isInstance);
        assertThat(components).anyMatch(DummyMob.class::isInstance);
        assertThat(components).anyMatch(GrassSpread.class::isInstance);
        assertThat(components).anyMatch(BuildingEffectReceiver.class::isInstance);

        assertThat(grassGenerator.getColliders())
                .filteredOn(CircleCollider.class::isInstance)
                .map(CircleCollider.class::cast)
                .anyMatch(collider -> collider.getRadius() == 5f && !collider.isTrigger());

        TimedSelfDestroyer selfDestroyer = components.stream()
                .filter(TimedSelfDestroyer.class::isInstance)
                .map(TimedSelfDestroyer.class::cast)
                .findFirst()
                .orElseThrow();
        GaugeDto gauge = selfDestroyer.getGauge();
        assertThat(gauge.maxValue()).isEqualTo(20f);
        assertThat(gauge.category()).isEqualTo(GaugeCategory.TTL);

        assertThat(grassGenerator.getElement().nativeHas(ElementType.NATURE)).isTrue();
    }

    private Stream<Component> components(GameObject gameObject) {
        return Stream.concat(
                gameObject.getComponents().stream(),
                gameObject.getComponentsToAdd().stream()
        );
    }
}
