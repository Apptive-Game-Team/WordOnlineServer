package com.wordonline.server.game.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.wordonline.server.game.domain.magic.CardType;

// PlayerData used to be locked because casts arrived on inbound and bot threads. They now arrive as
// game actions drained by the loop thread, so what is worth pinning is the accounting itself: a hand
// is charged once, a cast is rejected rather than half applied, and mana never goes negative.
class PlayerDataTest {

    private final Parameters parameters = mock(Parameters.class);

    private PlayerData newPlayer() {
        return new PlayerData(null, parameters);
    }

    @Test
    void useCardsConsumesTheHandAndChargesEveryCard() {
        when(parameters.getValue("fire", "mana_cost")).thenReturn(10.0);

        PlayerData player = newPlayer();
        player.mana = 20;
        player.addCard(CardType.Fire);
        player.addCard(CardType.Fire);

        assertThat(player.useCards(List.of(CardType.Fire, CardType.Fire))).isTrue();
        assertThat(player.mana).isZero();
        assertThat(player.cards).isEmpty();
    }

    @Test
    void useCardsRejectsAHandTheDeckCannotCoverWithoutChargingAnything() {
        when(parameters.getValue("fire", "mana_cost")).thenReturn(10.0);

        PlayerData player = newPlayer();
        player.mana = 20;
        player.addCard(CardType.Fire);

        assertThat(player.useCards(List.of(CardType.Fire, CardType.Fire))).isFalse();
        assertThat(player.mana).isEqualTo(20);
        assertThat(player.cards).containsExactly(CardType.Fire);
    }

    @Test
    void useCardsRejectsAHandTheManaPoolCannotCover() {
        when(parameters.getValue("fire", "mana_cost")).thenReturn(10.0);

        PlayerData player = newPlayer();
        player.mana = 5;
        player.addCard(CardType.Fire);

        assertThat(player.useCards(List.of(CardType.Fire))).isFalse();
        assertThat(player.mana).isEqualTo(5);
        assertThat(player.cards).containsExactly(CardType.Fire);
    }

    @Test
    void spendManaNeverOverdraws() {
        PlayerData player = newPlayer();
        player.mana = 10;

        assertThat(player.spendMana(10)).isTrue();
        assertThat(player.spendMana(10)).isFalse();
        assertThat(player.mana).isZero();
    }

    @Test
    void addManaClampsToTheMaximum() {
        PlayerData player = newPlayer();

        player.addMana(30, 20);

        assertThat(player.mana).isEqualTo(20);
    }

    @Test
    void addCardStopsAtTheHandLimit() {
        PlayerData player = newPlayer();

        for (int i = 0; i < PlayerData.MAX_CARD_NUM; i++) {
            assertThat(player.addCard(CardType.Fire)).isTrue();
        }

        assertThat(player.addCard(CardType.Fire)).isFalse();
        assertThat(player.cards).hasSize(PlayerData.MAX_CARD_NUM);
    }
}
