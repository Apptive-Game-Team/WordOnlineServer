package com.wordonline.server.game.domain;

import com.wordonline.server.game.domain.object.GameObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GameSessionDataConcurrencyTest {

    @Test
    void gameObjectsCanBeCopiedWhileTheLoopThreadMutatesThem() throws InterruptedException {
        GameSessionData sessionData = new GameSessionData(null, null);
        List<GameObject> objects = IntStream.range(0, 64)
                .mapToObj(i -> mock(GameObject.class))
                .toList();
        AtomicBoolean running = new AtomicBoolean(true);

        // Mimics GameObjectAddRemoteSystem.update on the loop thread.
        Thread loopThread = new Thread(() -> {
            while (running.get()) {
                sessionData.gameObjects.addAll(objects);
                sessionData.gameObjects.removeAll(objects);
            }
        });
        loopThread.start();

        try {
            // Mimics the BotEye copy on the bot executor thread: never a null slot,
            // never an exception, whatever the loop thread is doing mid-copy.
            long deadlineMillis = System.currentTimeMillis() + 500;
            while (System.currentTimeMillis() < deadlineMillis) {
                assertThat(new ArrayList<>(sessionData.gameObjects)).doesNotContainNull();
            }
        } finally {
            running.set(false);
            loopThread.join();
        }
    }
}
