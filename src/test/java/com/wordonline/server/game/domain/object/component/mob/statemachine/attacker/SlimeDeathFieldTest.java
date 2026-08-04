package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SlimeDeathFieldTest {

    @Test
    void lightningDeathDoesNotCreateLegacyDuplicateField() {
        GameContext gameContext = mock(GameContext.class);
        GameObject slimeObject = slimeObject(gameContext, ElementType.LIGHTNING);

        new Slime(slimeObject, 1, 1f, 0, 0, 1f).onDeath();

        verify(gameContext, never()).createGameObject(any(GameObject.class));
        assertThat(slimeObject.isDestroyed()).isTrue();
    }

    @Test
    void nonLightningElementalDeathFieldsRemainUnchanged() {
        GameContext gameContext = mock(GameContext.class);
        GameObject slimeObject = slimeObject(gameContext, ElementType.FIRE);

        new Slime(slimeObject, 1, 1f, 0, 0, 1f).onDeath();

        ArgumentCaptor<GameObject> created = ArgumentCaptor.forClass(GameObject.class);
        verify(gameContext).createGameObject(created.capture());
        assertThat(created.getValue().getType().name()).isEqualTo("FireField");
        assertThat(slimeObject.isDestroyed()).isTrue();
    }

    private GameObject slimeObject(GameContext gameContext, ElementType elementType) {
        GameObject slimeObject = new GameObject(
                Master.LeftPlayer,
                PrefabType.ElectricSlime,
                Vector3.ZERO,
                gameContext
        );
        slimeObject.setElement(elementType);
        clearInvocations(gameContext);
        return slimeObject;
    }
}
