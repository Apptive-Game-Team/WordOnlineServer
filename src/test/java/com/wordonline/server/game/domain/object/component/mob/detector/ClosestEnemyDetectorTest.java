package com.wordonline.server.game.domain.object.component.mob.detector;

import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.FrenzyStatusEffect;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClosestEnemyDetectorTest {

    @Test
    void filteredDetectionSkipsACloserEnemyOutsideTheAllowedArea() {
        GameObject self = mock(GameObject.class);
        GameObject closerFilteredOut = enemyAt(1f);
        GameObject fartherAllowed = enemyAt(2f);
        GameSessionData sessionData = new GameSessionData(null, null);
        sessionData.gameObjects.addAll(List.of(closerFilteredOut, fartherAllowed));
        GameContext gameContext = mock(GameContext.class);

        when(self.getMaster()).thenReturn(Master.LeftPlayer);
        when(self.getPosition()).thenReturn(Vector3.ZERO);
        when(gameContext.getGameSessionData()).thenReturn(sessionData);

        ClosestEnemyDetector detector = new ClosestEnemyDetector(gameContext, TargetMask.GROUND.bit);

        GameObject detected = detector.detect(self, target -> target == fartherAllowed);

        assertThat(detected).isSameAs(fartherAllowed);
    }

    @Test
    void frenziedMobCanTargetAnotherFrenziedMobWithTheSameNeutralMaster() {
        GameObject self = mock(GameObject.class);
        GameObject otherFrenziedMob = enemyAt(1f);
        FrenzyStatusEffect frenzy = mock(FrenzyStatusEffect.class);
        GameSessionData sessionData = new GameSessionData(null, null);
        sessionData.gameObjects.add(otherFrenziedMob);
        GameContext gameContext = mock(GameContext.class);

        when(self.getMaster()).thenReturn(Master.None);
        when(self.getPosition()).thenReturn(Vector3.ZERO);
        when(self.getComponent(FrenzyStatusEffect.class)).thenReturn(frenzy);
        when(frenzy.isActive()).thenReturn(true);
        when(otherFrenziedMob.getMaster()).thenReturn(Master.None);
        when(gameContext.getGameSessionData()).thenReturn(sessionData);

        ClosestEnemyDetector detector = new ClosestEnemyDetector(gameContext, TargetMask.GROUND.bit);

        assertThat(detector.detect(self)).isSameAs(otherFrenziedMob);
    }

    @Test
    void nonFrenziedMobStillIgnoresTargetsWithTheSameMaster() {
        GameObject self = mock(GameObject.class);
        GameObject ally = enemyAt(1f);
        GameSessionData sessionData = new GameSessionData(null, null);
        sessionData.gameObjects.add(ally);
        GameContext gameContext = mock(GameContext.class);

        when(self.getMaster()).thenReturn(Master.LeftPlayer);
        when(self.getPosition()).thenReturn(Vector3.ZERO);
        when(ally.getMaster()).thenReturn(Master.LeftPlayer);
        when(gameContext.getGameSessionData()).thenReturn(sessionData);

        ClosestEnemyDetector detector = new ClosestEnemyDetector(gameContext, TargetMask.GROUND.bit);

        assertThat(detector.detect(self)).isNull();
    }

    private GameObject enemyAt(float x) {
        GameObject enemy = mock(GameObject.class);
        when(enemy.getMaster()).thenReturn(Master.RightPlayer);
        when(enemy.getPosition()).thenReturn(new Vector3(x, 0f, 0f));
        when(enemy.getComponents()).thenReturn(List.of(mock(Mob.class)));
        return enemy;
    }
}
