package com.wordonline.server.game.domain.object.component.build;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.Element;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.ObjectsInfoDtoBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FlameBreathTest {

    private static final float ATTACK_INTERVAL = 1.5f;
    private static final float BEAM_WIDTH = 1f;
    private static final int DAMAGE = 30;
    private static final float BREATH_DURATION = 0.35f;
    private static final float MUZZLE_HEIGHT = 1.6f;

    private final GameContext gameContext = mock(GameContext.class);
    private final ObjectsInfoDtoBuilder dtoBuilder = mock(ObjectsInfoDtoBuilder.class);
    private final List<GameObject> activeGameObjects = new ArrayList<>();

    @Test
    void firesWithNothingInFrontOfIt() {
        GameObject tower = towerAt(new Vector3(4f, 0f, 5f), Master.LeftPlayer);

        breathe(tower);

        verify(dtoBuilder).createProjection(
                new Vector3(4f, MUZZLE_HEIGHT, 5f),
                new Vector3(18f, MUZZLE_HEIGHT, 5f),
                "FireShot",
                BREATH_DURATION);
        verify(tower).setStatus(Status.Attack);
    }

    @Test
    void burnsEveryEnemyInTheLaneAllTheWayToTheFieldEdge() {
        GameObject tower = towerAt(new Vector3(4f, 0f, 5f), Master.LeftPlayer);
        GameObject nearEnemy = enemyAt(new Vector3(6f, 0f, 5f));
        GameObject farEnemy = enemyAt(new Vector3(17f, 0f, 5f));
        GameObject airEnemy = enemyAt(new Vector3(10f, 3f, 5f));

        breathe(tower);

        verify(damageableOf(nearEnemy)).onDamaged(any(AttackInfo.class), eq(BREATH_DURATION));
        verify(damageableOf(farEnemy)).onDamaged(any(AttackInfo.class), eq(BREATH_DURATION));
        verify(damageableOf(airEnemy)).onDamaged(any(AttackInfo.class), eq(BREATH_DURATION));
    }

    @Test
    void leavesAloneWhatIsBehindItOrBesideTheLane() {
        GameObject tower = towerAt(new Vector3(4f, 0f, 5f), Master.LeftPlayer);
        GameObject behind = enemyAt(new Vector3(2f, 0f, 5f));
        GameObject besideTheLane = enemyAt(new Vector3(10f, 0f, 8f));
        GameObject ally = enemyAt(new Vector3(10f, 0f, 5f));
        when(ally.getMaster()).thenReturn(Master.LeftPlayer);

        breathe(tower);

        verify(damageableOf(behind), never()).onDamaged(any(AttackInfo.class), anyFloat());
        verify(damageableOf(besideTheLane), never()).onDamaged(any(AttackInfo.class), anyFloat());
        verify(damageableOf(ally), never()).onDamaged(any(AttackInfo.class), anyFloat());
    }

    @Test
    void facesTheOtherWayForTheRightPlayer() {
        GameObject tower = towerAt(new Vector3(14f, 0f, 5f), Master.RightPlayer);

        breathe(tower);

        verify(dtoBuilder).createProjection(
                new Vector3(14f, MUZZLE_HEIGHT, 5f),
                new Vector3(0f, MUZZLE_HEIGHT, 5f),
                "FireShot",
                BREATH_DURATION);
    }

    @Test
    void holdsItsFireUntilTheAttackIntervalHasPassed() {
        GameObject tower = towerAt(new Vector3(4f, 0f, 5f), Master.LeftPlayer);
        when(gameContext.getDeltaTime()).thenReturn(1f);
        FlameBreath flameBreath = new FlameBreath(tower, DAMAGE, ATTACK_INTERVAL, BEAM_WIDTH);

        flameBreath.update();

        verify(dtoBuilder, never()).createProjection(
                any(Vector3.class), any(Vector3.class), any(), anyFloat());

        flameBreath.update();

        verify(dtoBuilder).createProjection(
                any(Vector3.class), any(Vector3.class), eq("FireShot"), eq(BREATH_DURATION));
    }

    private void breathe(GameObject tower) {
        when(gameContext.getDeltaTime()).thenReturn(ATTACK_INTERVAL);
        new FlameBreath(tower, DAMAGE, ATTACK_INTERVAL, BEAM_WIDTH).update();
    }

    private GameObject towerAt(Vector3 position, Master master) {
        GameObject tower = mock(GameObject.class);
        Element fire = new Element();
        fire.addNative(ElementType.FIRE);
        when(tower.getPosition()).thenReturn(position);
        when(tower.getMaster()).thenReturn(master);
        when(tower.getElement()).thenReturn(fire);
        when(tower.getGameContext()).thenReturn(gameContext);
        when(gameContext.getObjectsInfoDtoBuilder()).thenReturn(dtoBuilder);
        when(gameContext.getActiveGameObjects()).thenReturn(activeGameObjects);
        activeGameObjects.add(tower);
        return tower;
    }

    private GameObject enemyAt(Vector3 position) {
        GameObject enemy = mock(GameObject.class);
        CircleCollider collider = mock(CircleCollider.class);
        when(enemy.isActive()).thenReturn(true);
        when(enemy.getMaster()).thenReturn(Master.RightPlayer);
        when(enemy.getPosition()).thenReturn(position);
        when(enemy.getComponent(Damageable.class)).thenReturn(mock(Damageable.class));
        when(enemy.getFirstCircleCollider()).thenReturn(Optional.of(collider));
        when(collider.getRadius()).thenReturn(0.25f);
        activeGameObjects.add(enemy);
        return enemy;
    }

    private Damageable damageableOf(GameObject gameObject) {
        return gameObject.getComponent(Damageable.class);
    }
}
