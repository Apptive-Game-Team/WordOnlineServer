package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LightningCloudTest {

    @Test
    void strikesImmediatelyThenTwiceAtTwoSecondIntervals() {
        GameObject cloudObject = mock(GameObject.class);
        GameContext gameContext = mock(GameContext.class);
        when(cloudObject.getGameContext()).thenReturn(gameContext);
        when(gameContext.getDeltaTime()).thenReturn(1.9f, 0.1f, 2f, 0.3f);
        RecordingLightningCloud cloud = new RecordingLightningCloud(cloudObject, 2f, 3);

        cloud.start();
        assertThat(cloud.strikes).isEqualTo(1);

        cloud.update();
        assertThat(cloud.strikes).isEqualTo(1);

        cloud.update();
        assertThat(cloud.strikes).isEqualTo(2);

        cloud.update();
        assertThat(cloud.strikes).isEqualTo(3);
        verify(cloudObject, times(3)).setStatus(Status.Attack);

        // the cloud outlives its last strike so the client can finish growing the bolt
        verify(cloudObject, never()).destroy();

        cloud.update();
        verify(cloudObject).destroy();
    }

    private static class RecordingLightningCloud extends LightningCloud {
        private int strikes;

        private RecordingLightningCloud(GameObject gameObject, float strikeInterval, int strikeCount) {
            super(gameObject, strikeInterval, strikeCount);
        }

        @Override
        protected void spawnLightningStrike() {
            strikes++;
        }
    }
}
