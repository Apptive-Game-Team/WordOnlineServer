package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.Element;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class TitanFistExplosionTest {

    @Test
    void damagesEnemiesInRangeAndIgnoresAllies() {
        GameObject fist = mock(GameObject.class);
        GameObject enemy = mock(GameObject.class);
        GameObject ally = mock(GameObject.class);
        Damageable enemyDamageable = mock(Damageable.class);
        Damageable allyDamageable = mock(Damageable.class);
        GameContext gameContext = mock(GameContext.class);

        when(fist.getGameContext()).thenReturn(gameContext);
        when(fist.getElement()).thenReturn(new Element());
        when(fist.getMaster()).thenReturn(Master.LeftPlayer);
        when(enemy.getMaster()).thenReturn(Master.RightPlayer);
        when(enemy.getComponents(Damageable.class)).thenReturn(List.of(enemyDamageable));
        when(ally.getMaster()).thenReturn(Master.LeftPlayer);
        when(ally.getComponents(Damageable.class)).thenReturn(List.of(allyDamageable));
        when(gameContext.getDeltaTime()).thenReturn(Explode.EXPLODE_DELAY);
        when(gameContext.overlapSphereAll(fist, 2f)).thenReturn(List.of(enemy, ally));

        new TitanFistExplosion(fist, 12, 2f).update();

        verify(enemyDamageable).onDamaged(any());
        verifyNoInteractions(allyDamageable);
        verify(fist).destroy();
    }
}
