package com.wordonline.server.game.service.system;

import com.wordonline.server.game.dto.input.InputRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects player inputs keyed by frame number.
 *
 * Thread-safety: receive() is called from the WebSocket handler thread;
 * consume() and isReady() are called from the game loop thread.
 * ConcurrentHashMap provides safe concurrent access.
 */
@Slf4j
@Component
@Scope("prototype")
public class InputBufferSystem {

    // buffer[frameNum][userId] = input
    private final Map<Integer, Map<Long, InputRequestDto>> buffer = new ConcurrentHashMap<>();

    /** Store an incoming input for the given frame. */
    public void receive(int frameNum, long userId, InputRequestDto input) {
        buffer.computeIfAbsent(frameNum, k -> new ConcurrentHashMap<>())
              .put(userId, input);
        log.trace("[InputBuffer] Received input frame={} userId={}", frameNum, userId);
    }

    /**
     * Returns true when all listed players have submitted inputs for the given frame,
     * OR the elapsed time since the frame started exceeds maxWaitMs.
     */
    public boolean isReady(int frameNum, List<Long> playerIds, long frameStartMs, long maxWaitMs) {
        Map<Long, InputRequestDto> frameInputs = buffer.getOrDefault(frameNum, Collections.emptyMap());
        boolean allSubmitted = playerIds.stream()
                .filter(id -> id >= 0) // skip bots (negative ids)
                .allMatch(frameInputs::containsKey);
        if (allSubmitted) return true;

        boolean timedOut = (System.currentTimeMillis() - frameStartMs) >= maxWaitMs;
        if (timedOut) {
            log.debug("[InputBuffer] Timeout for frame={} — missing players: {}",
                    frameNum,
                    playerIds.stream().filter(id -> id >= 0 && !frameInputs.containsKey(id)).toList());
        }
        return timedOut;
    }

    /**
     * Retrieves and removes the buffered inputs for the given frame.
     * Returns an empty map if no inputs were received (players took no action).
     */
    public Map<Long, InputRequestDto> consume(int frameNum) {
        Map<Long, InputRequestDto> inputs = buffer.remove(frameNum);
        return inputs != null ? inputs : Collections.emptyMap();
    }

    /** Discard any stale inputs for frames older than the given frame number. */
    public void discardOlderThan(int frameNum) {
        buffer.keySet().removeIf(f -> f < frameNum);
    }
}
