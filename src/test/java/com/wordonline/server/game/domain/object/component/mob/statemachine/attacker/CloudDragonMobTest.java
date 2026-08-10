package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CloudDragonMobTest {

    @Test
    void firesChainLightningOncePerFifteenSecondsAtCurrentTarget() {
        GameObject cloudDragon = mock(GameObject.class);
        GameObject target = mock(GameObject.class);
        GameContext gameContext = mock(GameContext.class);

        when(cloudDragon.getGameContext()).thenReturn(gameContext);
        when(cloudDragon.getMaster()).thenReturn(Master.LeftPlayer);
        when(target.getMaster()).thenReturn(Master.RightPlayer);
        when(target.getPosition()).thenReturn(new Vector3(3f, 0f, 0f));
        when(gameContext.getDeltaTime()).thenReturn(14.9f, 0.1f, 14.9f, 0.1f);

        RecordingCloudDragonMob mob = new RecordingCloudDragonMob(cloudDragon, 15f);
        mob.target = target;

        mob.update();
        assertThat(mob.shotCount).isZero();

        mob.update();
        assertThat(mob.shotCount).isEqualTo(1);
        assertThat(mob.lastTarget).isSameAs(target);

        mob.update();
        assertThat(mob.shotCount).isEqualTo(1);

        mob.update();
        assertThat(mob.shotCount).isEqualTo(2);
    }

    private static class RecordingCloudDragonMob extends CloudDragonMob {
        private int shotCount;
        private GameObject lastTarget;

        private RecordingCloudDragonMob(GameObject gameObject, float chainLightningCooldown) {
            super(gameObject, 30, 0.6f, 3, 5, 1f, 5f, chainLightningCooldown);
        }

        @Override
        protected void fireChainLightning(GameObject target) {
            shotCount++;
            lastTarget = target;
        }
    }
}
