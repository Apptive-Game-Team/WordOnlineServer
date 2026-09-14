package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CardSelectVisualizerTest {

    private static final long USER_ID = 7L;

    private final CardSelectVisualizer visualizer = new CardSelectVisualizer();
    private GameContext gameContext;
    private GameObject player;

    @BeforeEach
    void setUp() {
        gameContext = mock(GameContext.class);
        player = new GameObject(Master.LeftPlayer, PrefabType.Player, Vector3.ZERO, gameContext);
        when(gameContext.findPlayerGameObject(USER_ID)).thenReturn(Optional.of(player));
    }

    @Test
    void everyCardTypeMarksTheSelection() {
        for (CardType card : CardType.values()) {
            visualizer.selectCard(gameContext, USER_ID, card);
            assertThat(player.getEffects()).contains(Effect.CardSelected);
            visualizer.unselectAll(gameContext, USER_ID);
            player.flushComponents();
            assertThat(player.getEffects()).doesNotContain(Effect.CardSelected);
        }
    }

    @Test
    void magicCardsStillAddNoElementAura() {
        visualizer.selectCard(gameContext, USER_ID, CardType.Shoot);

        assertThat(player.getEffects()).containsExactly(Effect.CardSelected);
    }

    @Test
    void elementCardsKeepTheirIdleAura() {
        visualizer.selectCard(gameContext, USER_ID, CardType.Fire);
        assertThat(player.getEffects()).contains(Effect.FireIdleAura, Effect.CardSelected);

        visualizer.unselectCard(gameContext, USER_ID, CardType.Fire);
        assertThat(player.getEffects()).doesNotContain(Effect.FireIdleAura, Effect.CardSelected);
    }

    @Test
    void theMarkerStaysUntilTheLastCopyOfTheCardGoes() {
        visualizer.selectCard(gameContext, USER_ID, CardType.Fire);
        visualizer.selectCard(gameContext, USER_ID, CardType.Fire);
        visualizer.selectCard(gameContext, USER_ID, CardType.Shoot);

        visualizer.unselectCard(gameContext, USER_ID, CardType.Fire);
        assertThat(player.getEffects()).contains(Effect.CardSelected);

        visualizer.unselectCard(gameContext, USER_ID, CardType.Fire);
        assertThat(player.getEffects()).contains(Effect.CardSelected);

        visualizer.unselectCard(gameContext, USER_ID, CardType.Shoot);
        assertThat(player.getEffects()).doesNotContain(Effect.CardSelected);
    }

    @Test
    void aSuccessfulCastClearsTheMarker() {
        visualizer.selectCard(gameContext, USER_ID, CardType.Shoot);
        player.flushComponents();

        player.setStatus(Status.Attack);

        assertThat(player.getEffects()).doesNotContain(Effect.CardSelected);
    }

    @Test
    void anUnparsableMagicClearsTheMarker() {
        visualizer.selectCard(gameContext, USER_ID, CardType.Shoot);
        player.flushComponents();

        player.setStatus(Status.Hindered);

        assertThat(player.getEffects()).doesNotContain(Effect.CardSelected);
    }

    @Test
    void aCardSelectedInTheSameFrameAsACastBuildsAFreshMarker() {
        visualizer.selectCard(gameContext, USER_ID, CardType.Shoot);
        player.flushComponents();
        player.setStatus(Status.Attack);

        // the expired effect is still in the component list until the loop flushes removals
        visualizer.selectCard(gameContext, USER_ID, CardType.Shoot);
        assertThat(player.getEffects()).contains(Effect.CardSelected);

        visualizer.unselectCard(gameContext, USER_ID, CardType.Shoot);
        assertThat(player.getEffects()).doesNotContain(Effect.CardSelected);
    }
}
