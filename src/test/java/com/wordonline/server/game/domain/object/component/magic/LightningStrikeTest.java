package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.Element;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.util.Physics;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LightningStrikeTest {

    @Test
    void createsNeutralElectricFieldWhenStrikeTriggers() {
        GameObject strikeObject = mock(GameObject.class);
        GameContext gameContext = mock(GameContext.class);
        Physics physics = mock(Physics.class);
        Element lightning = new Element();
        lightning.addNative(ElementType.LIGHTNING);
        Vector3 strikePosition = new Vector3(4f, 10f, 6f);

        when(strikeObject.getGameContext()).thenReturn(gameContext);
        when(strikeObject.getElement()).thenReturn(lightning);
        when(strikeObject.getMaster()).thenReturn(Master.LeftPlayer);
        when(strikeObject.getPosition()).thenReturn(strikePosition);
        when(gameContext.getPhysics()).thenReturn(physics);
        when(physics.overlapBoxAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        new LightningStrike(strikeObject, 8, 0.5f).update();

        ArgumentCaptor<GameObject> createdObject = ArgumentCaptor.forClass(GameObject.class);
        verify(gameContext).createGameObject(createdObject.capture());
        assertThat(createdObject.getValue().getType()).isEqualTo(PrefabType.ElectricField);
        assertThat(createdObject.getValue().getMaster()).isEqualTo(Master.None);
        assertThat(createdObject.getValue().getPosition()).isEqualTo(new Vector3(4f, 0f, 6f));
    }
}
