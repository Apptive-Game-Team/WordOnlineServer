package com.wordonline.server.game.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.wordonline.server.game.domain.magic.CardType;

class PlayerDataConcurrencyTest {

    private static final int THREADS = 8;

    private final Parameters parameters = mock(Parameters.class);

    private PlayerData newPlayer() {
        return new PlayerData(null, parameters);
    }

    private void runConcurrently(Runnable task) throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        for (int i = 0; i < THREADS; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }
        start.countDown();
        done.await();
    }

    @Test
    void concurrentUseCardsConsumesTheHandOnlyOnce() throws InterruptedException {
        when(parameters.getValue("fire", "mana_cost")).thenReturn(10.0);

        PlayerData player = newPlayer();
        player.mana = 10;
        player.addCard(CardType.Fire);

        AtomicInteger successes = new AtomicInteger();
        runConcurrently(() -> {
            if (player.useCards(List.of(CardType.Fire))) {
                successes.incrementAndGet();
            }
        });

        assertThat(successes.get()).isEqualTo(1);
        assertThat(player.mana).isZero();
        assertThat(player.cards).isEmpty();
    }

    @Test
    void concurrentAddManaDoesNotLoseUpdates() throws InterruptedException {
        PlayerData player = newPlayer();

        runConcurrently(() -> {
            for (int i = 0; i < 1000; i++) {
                player.addMana(1, Integer.MAX_VALUE);
            }
        });

        assertThat(player.mana).isEqualTo(THREADS * 1000);
    }

    @Test
    void concurrentSpendManaNeverOverdraws() throws InterruptedException {
        PlayerData player = newPlayer();
        player.mana = 10;

        AtomicInteger successes = new AtomicInteger();
        runConcurrently(() -> {
            if (player.spendMana(10)) {
                successes.incrementAndGet();
            }
        });

        assertThat(successes.get()).isEqualTo(1);
        assertThat(player.mana).isZero();
    }
}
