package com.wordonline.server.game.domain.object.prefab.implement.rock;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.RockDeathRemnant;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
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

class RockRemnantPrefabInitializerTest {

    private static final float REMNANT_RADIUS = 0.75f;

    @Test
    void advertisesRockRemnantPrefabType() {
        RockRemnantPrefabInitializer initializer = new RockRemnantPrefabInitializer(mock(Parameters.class));

        assertThat(initializer.prefabType).isEqualTo(PrefabType.RockRemnant);
    }

    @Test
    void blocksPathWithImmovableSolidCollider() {
        GameObject remnant = initializedRemnant();

        CircleCollider collider = remnant.getFirstCircleCollider().orElseThrow();
        assertThat(collider.getRadius()).isEqualTo(REMNANT_RADIUS);
        assertThat(collider.isTrigger()).isFalse();
        assertThat(collider.getInvMass()).isZero();
        assertThat(components(remnant)).noneMatch(RigidBody.class::isInstance);
        assertThat(components(remnant)).anyMatch(Collidable.class::isInstance);
    }

    @Test
    void hasNoCombatBehaviour() {
        GameObject remnant = initializedRemnant();

        assertThat(components(remnant)).noneMatch(Mob.class::isInstance);
        assertThat(components(remnant)).noneMatch(Damageable.class::isInstance);
        assertThat(remnant.getElement().nativeHas(ElementType.ROCK)).isTrue();
    }

    @Test
    void expiresAfterTwentySeconds() {
        GameObject remnant = initializedRemnant();

        TimedSelfDestroyer selfDestroyer = components(remnant)
                .filter(TimedSelfDestroyer.class::isInstance)
                .map(TimedSelfDestroyer.class::cast)
                .findFirst()
                .orElseThrow();
        GaugeDto gauge = selfDestroyer.getGauge();
        assertThat(gauge.maxValue()).isEqualTo(20f);
        assertThat(gauge.category()).isEqualTo(GaugeCategory.TTL);
    }

    @Test
    void doesNotSpawnAnotherRemnant() {
        GameObject remnant = initializedRemnant();

        assertThat(components(remnant)).noneMatch(RockDeathRemnant.class::isInstance);
    }

    private GameObject initializedRemnant() {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters miniRockParameters = mock(GameObjectParameters.class);
        when(parameters.object(GameObjectKey.MINI_ROCK)).thenReturn(miniRockParameters);
        when(miniRockParameters.floatValue(ParameterKey.RADIUS)).thenReturn(REMNANT_RADIUS);
        GameObject remnant = new GameObject(
                Master.LeftPlayer,
                PrefabType.RockRemnant,
                Vector3.ZERO,
                mock(GameContext.class)
        );

        new RockRemnantPrefabInitializer(parameters).initialize(remnant);
        return remnant;
    }

    private Stream<Component> components(GameObject gameObject) {
        List<Component> all = Stream.concat(
                gameObject.getComponents().stream(),
                gameObject.getComponentsToAdd().stream()
        ).toList();
        return all.stream();
    }
}
