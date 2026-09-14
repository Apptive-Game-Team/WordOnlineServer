package com.wordonline.server.game.domain.object.component.magic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.simple.DummyMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.ObjectsInfoDtoBuilder;

class SpiritBombChannelTest {

    @Test
    void damagesClosestEnemyOnBeamAndPreservesExactFourTickTotal() {
        GameContext context = mock(GameContext.class);
        ObjectsInfoDtoBuilder dtoBuilder = mock(ObjectsInfoDtoBuilder.class);
        when(context.getObjectsInfoDtoBuilder()).thenReturn(dtoBuilder);
        when(context.getDeltaTime()).thenReturn(SpiritBombChannel.CHARGE_DURATION);

        GameObject player = object(Master.LeftPlayer, PrefabType.Player, Vector3.ZERO, context);
        GameObject nearEnemy = object(Master.RightPlayer, PrefabType.MiniRock, new Vector3(2f, 0f, 0f), context);
        DummyMob nearMob = addMob(nearEnemy);
        GameObject farEnemy = object(Master.RightPlayer, PrefabType.MiniRock, new Vector3(4f, 0f, 0f), context);
        DummyMob farMob = addMob(farEnemy);
        when(context.getActiveGameObjects()).thenReturn(List.of(player, farEnemy, nearEnemy));

        SpiritBombChannel channel = new SpiritBombChannel(
                player,
                new Vector3(6f, 0f, 0f),
                7
        );

        channel.update();
        when(context.getDeltaTime()).thenReturn(SpiritBombChannel.TICK_INTERVAL);
        channel.update();
        channel.update();
        channel.update();

        assertThat(nearMob.getHp()).isEqualTo(93);
        assertThat(farMob.getHp()).isEqualTo(100);
        assertThat(player.getComponentsToRemove()).contains(channel);
        verify(dtoBuilder, times(4)).createProjection(
                player,
                nearEnemy,
                SpiritBombChannel.BEAM_PROJECTILE,
                1.05f
        );
    }

    private DummyMob addMob(GameObject object) {
        DummyMob mob = new DummyMob(object, 100);
        object.getComponents().add(mob);
        object.addCollider(new CircleCollider(object, 0.5f, false));
        return mob;
    }

    private GameObject object(Master master, PrefabType type, Vector3 position, GameContext context) {
        GameObject object = new GameObject(master, type, position, context);
        object.setStatus(Status.Idle);
        return object;
    }
}
