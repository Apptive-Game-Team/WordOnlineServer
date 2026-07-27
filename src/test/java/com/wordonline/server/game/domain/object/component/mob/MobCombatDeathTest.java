package com.wordonline.server.game.domain.object.component.mob;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.CombatDeathListener;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MobCombatDeathTest {

    @Test
    void notifiesCombatDeathListenerOnlyWhenDamageIsLethal() {
        GameObject owner = owner();
        RecordingCombatDeathListener listener = new RecordingCombatDeathListener(owner);
        owner.addComponent(listener);
        owner.flushComponents();
        TestMob mob = new TestMob(owner, 10);

        mob.applyDamage(new AttackInfo(9, ElementType.NONE));
        assertThat(listener.notifications).isZero();

        mob.applyDamage(new AttackInfo(1, ElementType.NONE));
        assertThat(listener.notifications).isOne();
    }

    @Test
    void directDestroyDoesNotNotifyCombatDeathListener() {
        GameObject owner = owner();
        RecordingCombatDeathListener listener = new RecordingCombatDeathListener(owner);
        owner.addComponent(listener);
        owner.flushComponents();

        owner.destroy();

        assertThat(listener.notifications).isZero();
    }

    private GameObject owner() {
        GameContext gameContext = mock(GameContext.class);
        GameObject owner = new GameObject(Master.LeftPlayer, PrefabType.ZapMouse, Vector3.ZERO, gameContext);
        owner.setStatus(Status.Idle);
        owner.setElement(ElementType.LIGHTNING);
        return owner;
    }

    private static class RecordingCombatDeathListener extends Component implements CombatDeathListener {
        private int notifications;

        private RecordingCombatDeathListener(GameObject gameObject) {
            super(gameObject);
        }

        @Override
        public void onCombatDeath() {
            notifications++;
        }

        @Override
        public void start() {
        }

        @Override
        public void update() {
        }

        @Override
        public void onDestroy() {
        }
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
