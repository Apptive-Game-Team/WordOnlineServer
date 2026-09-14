package com.wordonline.server.game.domain.object.prefab.implement.build;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.build.FlameBreath;
import com.wordonline.server.game.domain.object.component.effect.RockDeathRemnant;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.simple.DummyMob;
import com.wordonline.server.game.domain.object.component.mob.simple.Tower;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.GameObjectParameters;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DragonTowerPrefabInitializerTest {

    @Test
    void advertisesDragonTowerPrefabType() {
        DragonTowerPrefabInitializer initializer = new DragonTowerPrefabInitializer(mock(Parameters.class));

        assertThat(initializer.prefabType).isEqualTo(PrefabType.DragonTower);
    }

    @Test
    void buildsATowerThatBreathesForwardOnATimerAndExpiresAfterDuration() {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters dragonTowerParameters = mock(GameObjectParameters.class);
        when(parameters.object(GameObjectKey.DRAGON_TOWER)).thenReturn(dragonTowerParameters);
        when(dragonTowerParameters.floatValue(ParameterKey.RADIUS)).thenReturn(0.8f);
        when(dragonTowerParameters.intValue(ParameterKey.HP)).thenReturn(120);
        when(dragonTowerParameters.intValue(ParameterKey.DAMAGE)).thenReturn(30);
        when(dragonTowerParameters.floatValue(ParameterKey.ATTACK_INTERVAL)).thenReturn(1.5f);
        when(dragonTowerParameters.floatValue(ParameterKey.BEAM_WIDTH)).thenReturn(1f);
        when(dragonTowerParameters.floatValue(ParameterKey.DURATION)).thenReturn(20f);

        GameObject dragonTower = new GameObject(
                Master.LeftPlayer,
                PrefabType.DragonTower,
                Vector3.ZERO,
                mock(GameContext.class)
        );

        new DragonTowerPrefabInitializer(parameters).initialize(dragonTower);

        assertThat(dragonTower.getElement().has(ElementType.FIRE)).isTrue();

        CircleCollider collider = dragonTower.getFirstCircleCollider().orElseThrow();
        assertThat(collider.getRadius()).isEqualTo(0.8f);
        assertThat(collider.isTrigger()).isFalse();

        DummyMob mob = findComponent(dragonTower, DummyMob.class);
        assertThat(mob).isNotNull();
        assertThat(mob.getHp()).isEqualTo(120);
        assertThat(mob.getMaxHp()).isEqualTo(120);

        FlameBreath flameBreath = findComponent(dragonTower, FlameBreath.class);
        assertThat(flameBreath).isNotNull();
        assertThat(flameBreath.getAttackInterval().total()).isEqualTo(1.5f);

        TimedSelfDestroyer selfDestroyer = findComponent(dragonTower, TimedSelfDestroyer.class);
        assertThat(selfDestroyer).isNotNull();
        assertThat(selfDestroyer.getGauge().maxValue()).isEqualTo(20f);

        assertThat(findComponent(dragonTower, RockDeathRemnant.class)).isNotNull();
        assertThat(findComponent(dragonTower, CommonEffectReceiver.class)).isNotNull();
    }

    // dragon_tower no longer picks a target, so it must not carry the detector-driven Tower
    // component that every other tower uses.
    @Test
    void doesNotAttachTheTargetingTowerComponent() {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters dragonTowerParameters = mock(GameObjectParameters.class);
        when(parameters.object(GameObjectKey.DRAGON_TOWER)).thenReturn(dragonTowerParameters);

        GameObject dragonTower = new GameObject(
                Master.LeftPlayer,
                PrefabType.DragonTower,
                Vector3.ZERO,
                mock(GameContext.class)
        );

        new DragonTowerPrefabInitializer(parameters).initialize(dragonTower);

        assertThat(findComponent(dragonTower, Tower.class)).isNull();
    }

    // Some components land in `components` and others in the pending `componentsToAdd` queue
    // depending on how the initializer attaches them, so tests look in both.
    private <T> T findComponent(GameObject gameObject, Class<T> clazz) {
        return Stream.concat(gameObject.getComponents().stream(), gameObject.getComponentsToAdd().stream())
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }
}
