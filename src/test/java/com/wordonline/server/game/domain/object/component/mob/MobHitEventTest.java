package com.wordonline.server.game.domain.object.component.mob;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.dto.frame.GameEventDto;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MobHitEventTest {

    @Test
    void publishesHitEventWhenDamageLands() {
        GameContext gameContext = mock(GameContext.class);
        GameObject attacker = gameObject(gameContext, Master.LeftPlayer);
        GameObject target = gameObject(gameContext, Master.RightPlayer);
        TestMob mob = new TestMob(target, 10);

        mob.applyDamage(new AttackInfo(3, ElementType.NONE).withAttacker(attacker));

        verify(gameContext).addEvent(GameEventDto.hit(attacker.getId(), target.getId()));
    }

    @Test
    void publishesNothingWhenDamageHasNoAttacker() {
        GameContext gameContext = mock(GameContext.class);
        GameObject target = gameObject(gameContext, Master.RightPlayer);
        TestMob mob = new TestMob(target, 10);

        mob.applyDamage(new AttackInfo(3, ElementType.NONE));

        verify(gameContext, never()).addEvent(ArgumentMatchers.any());
    }

    @Test
    void publishesNothingForHeals() {
        GameContext gameContext = mock(GameContext.class);
        GameObject healer = gameObject(gameContext, Master.LeftPlayer);
        GameObject target = gameObject(gameContext, Master.LeftPlayer);
        TestMob mob = new TestMob(target, 10);

        mob.applyDamage(new AttackInfo(-3, ElementType.NONE).withAttacker(healer));

        verify(gameContext, never()).addEvent(ArgumentMatchers.any());
    }

    private GameObject gameObject(GameContext gameContext, Master master) {
        GameObject gameObject = new GameObject(master, PrefabType.ZapMouse, Vector3.ZERO, gameContext);
        gameObject.setStatus(Status.Idle);
        gameObject.setElement(ElementType.NONE);
        return gameObject;
    }

    private static class TestMob extends Mob {
        private TestMob(GameObject gameObject, int maxHp) {
            super(gameObject, maxHp, 1f);
        }

        @Override
        public void onDeath() {
            gameObject.destroy();
        }

        @Override
        public void start() {
        }

        @Override
        public void onDestroy() {
        }
    }
}
