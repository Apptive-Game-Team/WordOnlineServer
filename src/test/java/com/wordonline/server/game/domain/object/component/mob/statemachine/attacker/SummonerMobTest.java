package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SummonerMobTest {

    @Test
    void summonsOwnedPrefabAtTargetPositionWhenTargetIsWithinRange() {
        GameContext gameContext = mock(GameContext.class);
        GameObject summonerObject = new GameObject(
                Master.LeftPlayer,
                PrefabType.MagmaSpirit,
                new Vector3(2f, 0f, 3f),
                gameContext
        );
        GameObject target = new GameObject(
                Master.RightPlayer,
                PrefabType.FireSlime,
                new Vector3(3.2f, 0f, 4.6f),
                gameContext
        );
        clearInvocations(gameContext);
        when(gameContext.getDeltaTime()).thenReturn(1.1f);

        SummonerMob summonerMob = new SummonerMob(
                summonerObject,
                10,
                1f,
                1,
                1f,
                5f,
                PrefabType.MagmaFist
        );
        summonerMob.target = target;
        summonerMob.setState(summonerMob.new AttackState());

        summonerMob.update();

        ArgumentCaptor<GameObject> created = ArgumentCaptor.forClass(GameObject.class);
        verify(gameContext).createGameObject(created.capture());
        GameObject summonedObject = created.getValue();
        assertThat(summonedObject.getType()).isEqualTo(PrefabType.MagmaFist);
        assertThat(summonedObject.getMaster()).isEqualTo(Master.LeftPlayer);
        assertThat(summonedObject.getPosition()).isEqualTo(target.getPosition());
    }

    @Test
    void summonsOwnedPrefabAtMaximumRangeWhenTargetCenterIsOutsideRange() {
        GameContext gameContext = mock(GameContext.class);
        GameObject summonerObject = new GameObject(
                Master.LeftPlayer,
                PrefabType.MagmaSpirit,
                new Vector3(2f, 0f, 3f),
                gameContext
        );
        GameObject target = new GameObject(
                Master.RightPlayer,
                PrefabType.FireSlime,
                new Vector3(8f, 0f, 11f),
                gameContext
        );
        target.addCollider(new CircleCollider(target, 6f, false));
        clearInvocations(gameContext);
        when(gameContext.getDeltaTime()).thenReturn(1.1f);

        SummonerMob summonerMob = new SummonerMob(
                summonerObject,
                10,
                1f,
                1,
                1f,
                5f,
                PrefabType.MagmaFist
        );
        summonerMob.target = target;
        summonerMob.targetRadius = 6f;
        summonerMob.setState(summonerMob.new AttackState());

        summonerMob.update();

        ArgumentCaptor<GameObject> created = ArgumentCaptor.forClass(GameObject.class);
        verify(gameContext).createGameObject(created.capture());
        GameObject summonedObject = created.getValue();
        assertThat(summonedObject.getType()).isEqualTo(PrefabType.MagmaFist);
        assertThat(summonedObject.getMaster()).isEqualTo(Master.LeftPlayer);
        assertThat(summonedObject.getPosition().getX()).isCloseTo(5f, within(0.0001f));
        assertThat(summonedObject.getPosition().getY()).isZero();
        assertThat(summonedObject.getPosition().getZ()).isCloseTo(7f, within(0.0001f));
        assertThat(summonedObject.getPosition().distance(summonerObject.getPosition()))
                .isCloseTo(5d, within(0.0001d));
    }
}
