package com.wordonline.server.game.service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

// Many producers (the STOMP inbound threads, the bot executor, the ping timeout scheduler), one
// consumer (the loop thread). Producers only enqueue; every queued action runs on the loop thread
// at the top of a frame, which is what lets the rest of the game state be written without locks.
@Slf4j
public class GameActionQueue {

    // A well behaved client sends a handful of inputs per frame. Anything past this is a flood, and
    // an unbounded queue would let it grow the session's heap for as long as the match runs.
    static final int CAPACITY = 256;

    private final Queue<QueuedAction> actions = new ConcurrentLinkedQueue<>();
    private final AtomicInteger size = new AtomicInteger();

    // Returns false when the queue is full; the action is dropped and never runs.
    public boolean submit(String name, Runnable action) {
        int current;
        do {
            current = size.get();
            if (current >= CAPACITY) {
                log.warn("game action queue is full ({}), dropping action '{}'", CAPACITY, name);
                return false;
            }
        } while (!size.compareAndSet(current, current + 1));

        actions.add(new QueuedAction(name, action));
        return true;
    }

    // Runs the actions that were already queued when the frame started. An action that queues
    // another action leaves it for the next frame rather than extending this one indefinitely.
    // A failing action must not take the frame down with it, so each one is caught on its own.
    public void drain() {
        for (int remaining = size.get(); remaining > 0; remaining--) {
            QueuedAction queued = actions.poll();
            if (queued == null) {
                return;
            }
            size.decrementAndGet();

            try {
                queued.action().run();
            } catch (Exception e) {
                log.error("game action '{}' failed", queued.name(), e);
            }
        }
    }

    public int size() {
        return size.get();
    }

    private record QueuedAction(String name, Runnable action) {
    }
}
