package com.wordonline.server.game.domain.object.component.mob;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.CombatDeathListener;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetRelation;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AerialDeathFallTest {

    private static final int MAX_TICKS = 200;

    @Test
    void aerialMobKeepsFallingBeforeItIsRemoved() {
        GameContext gameContext = gameContext();
        GameObject gameObject = aerialObject(gameContext, GameConfig.AERIAL_MOB_INIT_HEIGHT);
        ZPhysics zPhysics = gameObject.getComponent(ZPhysics.class);
        TestMob mob = mob(gameObject, 10);
        RecordingCombatDeathListener listener = listener(gameObject);

        mob.applyDamage(new AttackInfo(10, ElementType.NONE));

        assertThat(gameObject.getStatus()).isEqualTo(Status.Dying);
        assertThat(gameObject.isDestroyed()).isFalse();
        assertThat(listener.notifications).isZero();

        int ticks = simulateUntilDestroyed(gameObject, zPhysics);

        assertThat(gameObject.isDestroyed()).isTrue();
        assertThat(gameObject.getPosition().getY()).isCloseTo(zPhysics.getGroundY(), within(0.01f));
        assertThat(listener.notifications).isOne();
        // the fall has to take a noticeable amount of frames, not a single one
        assertThat(ticks).isGreaterThan(10);
    }

    @Test
    void dyingAerialMobStopsActingAndCannotBeHitOrTargeted() {
        GameContext gameContext = gameContext();
        GameObject gameObject = aerialObject(gameContext, GameConfig.AERIAL_MOB_INIT_HEIGHT);
        ZPhysics zPhysics = gameObject.getComponent(ZPhysics.class);
        TestMob mob = mob(gameObject, 10);
        CountingComponent behavior = new CountingComponent(gameObject);
        gameObject.addComponent(behavior);
        gameObject.flushComponents();

        mob.applyDamage(new AttackInfo(10, ElementType.NONE));
        int hpOnDeath = mob.getHp();

        tick(gameObject, zPhysics);
        tick(gameObject, zPhysics);

        mob.onDamaged(new AttackInfo(5, ElementType.NONE));

        assertThat(mob.getHp()).isEqualTo(hpOnDeath);
        assertThat(gameObject.getStatus()).isEqualTo(Status.Dying);
        assertThat(behavior.updates).isZero();
        assertThat(TargetRelation.canAttack(enemyOf(gameContext), gameObject)).isFalse();
    }

    @Test
    void dyingStatusIsKeptUntilTheMobIsRemoved() {
        GameContext gameContext = gameContext();
        GameObject gameObject = aerialObject(gameContext, GameConfig.AERIAL_MOB_INIT_HEIGHT);
        TestMob mob = mob(gameObject, 10);

        mob.applyDamage(new AttackInfo(10, ElementType.NONE));
        gameObject.setStatus(Status.Idle);
        gameObject.setStatus(Status.Damaged);

        assertThat(gameObject.getStatus()).isEqualTo(Status.Dying);

        gameObject.destroy();

        assertThat(gameObject.getStatus()).isEqualTo(Status.Destroyed);
    }

    @Test
    void groundMobIsRemovedImmediately() {
        GameContext gameContext = gameContext();
        GameObject gameObject = object(gameContext, PrefabType.ZapMouse, Vector3.ZERO);
        gameObject.addComponent(new ZPhysics(gameObject));
        gameObject.flushComponents();
        TestMob mob = mob(gameObject, 10);
        RecordingCombatDeathListener listener = listener(gameObject);

        mob.applyDamage(new AttackInfo(10, ElementType.NONE));

        assertThat(gameObject.getStatus()).isEqualTo(Status.Destroyed);
        assertThat(listener.notifications).isOne();
    }

    @Test
    void aerialMobAlreadyOnTheGroundIsRemovedImmediately() {
        GameContext gameContext = gameContext();
        GameObject gameObject = aerialObject(gameContext, 0f);
        TestMob mob = mob(gameObject, 10);
        RecordingCombatDeathListener listener = listener(gameObject);

        mob.applyDamage(new AttackInfo(10, ElementType.NONE));

        assertThat(gameObject.getStatus()).isEqualTo(Status.Destroyed);
        assertThat(listener.notifications).isOne();
    }

    private int simulateUntilDestroyed(GameObject gameObject, ZPhysics zPhysics) {
        int ticks = 0;
        while (!gameObject.isDestroyed() && ticks < MAX_TICKS) {
            tick(gameObject, zPhysics);
            ticks++;
        }
        return ticks;
    }

    // mimics the tick order of ComponentUpdateSystem, PhysicSystem and GameObjectAddRemoteSystem
    private void tick(GameObject gameObject, ZPhysics zPhysics) {
        gameObject.update();
        if (!gameObject.isActive()) {
            return;
        }
        if (zPhysics.canHover()) {
            zPhysics.applyHover();
        } else {
            zPhysics.applyZForce();
        }
        gameObject.flushComponents();
    }

    private GameContext gameContext() {
        GameContext gameContext = mock(GameContext.class);
        when(gameContext.getDeltaTime()).thenReturn(0.05f);
        return gameContext;
    }

    private GameObject aerialObject(GameContext gameContext, float height) {
        GameObject gameObject = object(gameContext, PrefabType.ThunderSpirit, new Vector3(9, height, 5));
        gameObject.addComponent(new ZPhysics(gameObject, height));
        gameObject.flushComponents();
        return gameObject;
    }

    private GameObject enemyOf(GameContext gameContext) {
        return object(gameContext, PrefabType.ZapMouse, new Vector3(9, 0, 5), Master.RightPlayer);
    }

    private GameObject object(GameContext gameContext, PrefabType type, Vector3 position) {
        return object(gameContext, type, position, Master.LeftPlayer);
    }

    private GameObject object(GameContext gameContext, PrefabType type, Vector3 position, Master master) {
        GameObject gameObject = new GameObject(master, type, position, gameContext);
        gameObject.setStatus(Status.Idle);
        gameObject.setElement(ElementType.LIGHTNING);
        return gameObject;
    }

    private TestMob mob(GameObject gameObject, int maxHp) {
        TestMob mob = new TestMob(gameObject, maxHp);
        gameObject.addComponent(mob);
        gameObject.flushComponents();
        return mob;
    }

    private RecordingCombatDeathListener listener(GameObject gameObject) {
        RecordingCombatDeathListener listener = new RecordingCombatDeathListener(gameObject);
        gameObject.addComponent(listener);
        gameObject.flushComponents();
        return listener;
    }

    private static class CountingComponent extends Component {
        private int updates;

        private CountingComponent(GameObject gameObject) {
            super(gameObject);
        }

        @Override
        public void start() {
        }

        @Override
        public void update() {
            updates++;
        }

        @Override
        public void onDestroy() {
        }
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
