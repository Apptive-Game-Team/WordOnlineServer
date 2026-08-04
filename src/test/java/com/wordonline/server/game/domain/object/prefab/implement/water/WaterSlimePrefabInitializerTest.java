package com.wordonline.server.game.domain.object.prefab.implement.water;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.PathSpawner;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.ProjectileRangeAttackMob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.Slime;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.WaterSlimeRangeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.GameObjectParameters;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WaterSlimePrefabInitializerTest {

    @Test
    void initializesProjectileRangedAttackFromExistingWaterSlimeAndAquaArcherParameters() {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters slime = mock(GameObjectParameters.class);
        GameObjectParameters aquaArcher = mock(GameObjectParameters.class);
        GameObject waterSlime = mock(GameObject.class);
        List<Component> components = new ArrayList<>();

        when(parameters.object(GameObjectKey.WATER_SLIME)).thenReturn(slime);
        when(parameters.object(GameObjectKey.AQUA_ARCHER)).thenReturn(aquaArcher);
        when(slime.intValue(ParameterKey.MASS)).thenReturn(3);
        when(slime.floatValue(ParameterKey.RADIUS)).thenReturn(0.75f);
        when(slime.intValue(ParameterKey.HP)).thenReturn(11);
        when(slime.floatValue(ParameterKey.SPEED)).thenReturn(1.25f);
        when(slime.intValue(ParameterKey.DAMAGE)).thenReturn(4);
        when(slime.floatValue(ParameterKey.ATTACK_INTERVAL)).thenReturn(1.5f);
        when(aquaArcher.floatValue(ParameterKey.ATTACK_RANGE)).thenReturn(6f);
        when(waterSlime.getPosition()).thenReturn(Vector3.ZERO);
        when(waterSlime.getGameContext()).thenReturn(mock(GameContext.class));
        when(waterSlime.getComponents()).thenReturn(components);

        new WaterSlimePrefabInitializer(parameters).initialize(waterSlime);

        assertThat(components).noneMatch(Slime.class::isInstance);
        ProjectileRangeAttackMob rangedAttack = components.stream()
                .filter(ProjectileRangeAttackMob.class::isInstance)
                .map(ProjectileRangeAttackMob.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(rangedAttack).isInstanceOf(WaterSlimeRangeAttackMob.class);
        assertThat(rangedAttack.getMaxHp()).isEqualTo(11);
        assertThat(rangedAttack.getSpeed().total()).isEqualTo(1.25f);
        assertThat(rangedAttack.getAttackInterval().total()).isEqualTo(1.5f);
        assertThat(ReflectionTestUtils.getField(rangedAttack, "attackRange")).isEqualTo(6f);
        Object detector = ReflectionTestUtils.getField(rangedAttack, "detector");
        assertThat(ReflectionTestUtils.getField(detector, "targetMask")).isEqualTo(TargetMask.GROUND.bit);

        RigidBody rigidBody = components.stream()
                .filter(RigidBody.class::isInstance)
                .map(RigidBody.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(rigidBody.getMass()).isEqualTo(3);

        ArgumentCaptor<CircleCollider> collider = ArgumentCaptor.forClass(CircleCollider.class);
        verify(waterSlime).addCollider(collider.capture());
        assertThat(collider.getValue().getRadius()).isEqualTo(0.75f);
        assertThat(collider.getValue().isTrigger()).isFalse();

        PathSpawner pathSpawner = components.stream()
                .filter(PathSpawner.class::isInstance)
                .map(PathSpawner.class::cast)
                .findFirst()
                .orElseThrow();
        assertThat(ReflectionTestUtils.getField(pathSpawner, "prefabType")).isEqualTo(PrefabType.WaterField);
        assertThat(ReflectionTestUtils.getField(pathSpawner, "interval")).isEqualTo(1f);

        verify(parameters).object(GameObjectKey.WATER_SLIME);
        verify(parameters).object(GameObjectKey.AQUA_ARCHER);
        verify(slime).intValue(ParameterKey.MASS);
        verify(slime).floatValue(ParameterKey.RADIUS);
        verify(slime).intValue(ParameterKey.HP);
        verify(slime).floatValue(ParameterKey.SPEED);
        verify(slime).intValue(ParameterKey.DAMAGE);
        verify(slime).floatValue(ParameterKey.ATTACK_INTERVAL);
        verify(aquaArcher).floatValue(ParameterKey.ATTACK_RANGE);
    }
}
