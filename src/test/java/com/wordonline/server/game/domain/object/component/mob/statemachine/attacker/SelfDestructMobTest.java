package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.util.Physics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelfDestructMobTest {

    @Test
    void sameAltitudeTargetIsInterceptedInsteadOfProducingNaNPosition() {
        GameContext gameContext = mock(GameContext.class);
        Physics physics = mock(Physics.class);
        when(gameContext.getPhysics()).thenReturn(physics);

        GameObject windSpirit = new GameObject(Master.LeftPlayer, PrefabType.WindSpirit,
                new Vector3(4f, GameConfig.AERIAL_MOB_INIT_HEIGHT, 4f), gameContext);
        windSpirit.addCollider(new CircleCollider(windSpirit, 0.5f, false));
        GameObject enemy = new GameObject(Master.RightPlayer, PrefabType.BubbleSpirit,
                new Vector3(6f, GameConfig.AERIAL_MOB_INIT_HEIGHT, 4f), gameContext);
        enemy.addCollider(new CircleCollider(enemy, 0.5f, false));

        SelfDestructMob enemyMob = new SelfDestructMob(
                enemy, 10, 1f, TargetMask.AIR.bit, 0, 1f, 1f);
        enemy.getComponents().add(enemyMob);
        when(physics.overlapSphereAll(any(GameObject.class), anyFloat())).thenReturn(List.of(enemy));

        SelfDestructMob windSpiritMob = new SelfDestructMob(
                windSpirit, 10, 1f, TargetMask.AIR.bit, 7, 1f, 2f);
        windSpirit.getComponents().add(windSpiritMob);
        windSpiritMob.start();
        windSpiritMob.setState(windSpiritMob.new AttackingState(enemyMob));

        windSpiritMob.update();

        assertThat(windSpirit.getPosition().hasNaN()).isFalse();
        assertThat(windSpirit.getPosition().getX()).isEqualTo(6f);
        assertThat(enemyMob.getHp()).isLessThan(10);
        assertThat(windSpirit.isDestroyed()).isTrue();
    }
}
