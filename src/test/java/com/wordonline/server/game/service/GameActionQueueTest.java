package com.wordonline.server.game.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

// The queue is the only thing standing between the loop thread and every other thread that wants to
// change game state, so the properties the rest of the refactor relies on are pinned here.
class GameActionQueueTest {

    @Test
    void runsQueuedActionsInSubmissionOrder() {
        GameActionQueue queue = new GameActionQueue();
        List<String> executed = new ArrayList<>();

        queue.submit("first", () -> executed.add("first"));
        queue.submit("second", () -> executed.add("second"));

        assertThat(executed).isEmpty();

        queue.drain();

        assertThat(executed).containsExactly("first", "second");
        assertThat(queue.size()).isZero();
    }

    // Without this a cast that queues another cast could keep the frame running forever.
    @Test
    void leavesActionsQueuedDuringADrainForTheNextDrain() {
        GameActionQueue queue = new GameActionQueue();
        List<String> executed = new ArrayList<>();

        queue.submit("outer", () -> {
            executed.add("outer");
            queue.submit("inner", () -> executed.add("inner"));
        });

        queue.drain();
        assertThat(executed).containsExactly("outer");

        queue.drain();
        assertThat(executed).containsExactly("outer", "inner");
    }

    // A single bad cast must not take the frame - or the rest of the queue - down with it.
    @Test
    void keepsDrainingAfterAnActionThrows() {
        GameActionQueue queue = new GameActionQueue();
        List<String> executed = new ArrayList<>();

        queue.submit("boom", () -> {
            throw new IllegalStateException("boom");
        });
        queue.submit("after", () -> executed.add("after"));

        queue.drain();

        assertThat(executed).containsExactly("after");
    }

    @Test
    void rejectsSubmissionsOnceFullSoAFloodCannotGrowTheSession() {
        GameActionQueue queue = new GameActionQueue();
        AtomicInteger runs = new AtomicInteger();

        for (int i = 0; i < GameActionQueue.CAPACITY; i++) {
            assertThat(queue.submit("fill", runs::incrementAndGet)).isTrue();
        }

        assertThat(queue.submit("overflow", runs::incrementAndGet)).isFalse();

        queue.drain();
        assertThat(runs).hasValue(GameActionQueue.CAPACITY);
    }

    @Test
    void acceptsConcurrentSubmissionsWithoutLosingAny() throws InterruptedException {
        GameActionQueue queue = new GameActionQueue();
        int producers = 8;
        int perProducer = 20;
        AtomicInteger runs = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(producers);

        for (int i = 0; i < producers; i++) {
            Thread producer = new Thread(() -> {
                try {
                    start.await();
                    for (int j = 0; j < perProducer; j++) {
                        queue.submit("concurrent", runs::incrementAndGet);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
            producer.start();
        }

        start.countDown();
        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();

        queue.drain();
        assertThat(runs).hasValue(producers * perProducer);
    }
}
