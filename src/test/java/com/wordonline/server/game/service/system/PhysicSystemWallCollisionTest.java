package com.wordonline.server.game.service.system;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.effect.receiver.LightningSummonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.CowardMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.EdgeCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.WallCollision;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PhysicSystemWallCollisionTest {

    @Test
    void mapWallReflectsCowardDuringActualPanicState() {
        GameContext gameContext = mock(GameContext.class);
        GameObject unit = new GameObject(
                Master.LeftPlayer,
                PrefabType.ZapMouse,
                new Vector3(0.4f, 0f, 5f),
                gameContext);
        RigidBody rigidBody = new RigidBody(unit, 2);
        CowardMob cowardMob = new CowardMob(
                unit,
                10,
                2f,
                TargetMask.GROUND.bit,
                1,
                1f,
                3f,
                1f);
        unit.addComponent(rigidBody);
        unit.addComponent(cowardMob);
        unit.addComponent(new LightningSummonEffectReceiver(unit));
        unit.addCollider(new CircleCollider(unit, 0.5f, false));
        unit.flushComponents();
        unit.setStatus(Status.Idle);

        GameObject threat = new GameObject(
                Master.RightPlayer,
                PrefabType.FireSpirit,
                new Vector3(2f, 0f, 5f),
                gameContext);
        cowardMob.setState(cowardMob.new PanicState(threat));

        GameObject wall = new GameObject(Master.None, PrefabType.Wall, Vector3.ZERO, gameContext);
        wall.addComponent(new WallCollision(wall));
        wall.addCollider(new EdgeCollider(
                wall,
                new Vector3(0f, 0f, 0f),
                new Vector3(0f, 0f, 10f),
                false));
        wall.flushComponents();
        wall.setStatus(Status.Idle);

        when(gameContext.getActiveGameObjects()).thenReturn(List.of(unit, wall));
        when(gameContext.getDeltaTime()).thenReturn(0.1f);

        unit.update();
        assertThat(unit.getEffects()).contains(Effect.Panic);

        new PhysicSystem().update(gameContext);

        assertThat(unit.isDestroyed()).isFalse();
        assertThat(unit.getPosition().getX()).isGreaterThan(0.4f);
    }
}
