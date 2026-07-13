package com.wordonline.server.game.service.lockstep;

import com.wordonline.server.game.dto.lockstep.LockstepAbortReason;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

@Component
public final class LockstepMetrics {
    private final LongAdder startedSessions = new LongAdder();
    private final LongAdder confirmedFrames = new LongAdder();
    private final Map<LockstepAbortReason, LongAdder> abortedSessions = new EnumMap<>(LockstepAbortReason.class);

    public LockstepMetrics() {
        for (LockstepAbortReason reason : LockstepAbortReason.values()) {
            abortedSessions.put(reason, new LongAdder());
        }
    }

    public void sessionStarted() { startedSessions.increment(); }
    public void frameConfirmed() { confirmedFrames.increment(); }
    public void sessionAborted(LockstepAbortReason reason) { abortedSessions.get(reason).increment(); }

    public Snapshot snapshot() {
        Map<LockstepAbortReason, Long> aborts = new EnumMap<>(LockstepAbortReason.class);
        abortedSessions.forEach((reason, count) -> aborts.put(reason, count.sum()));
        return new Snapshot(startedSessions.sum(), confirmedFrames.sum(), Map.copyOf(aborts));
    }

    public record Snapshot(long startedSessions, long confirmedFrames,
                           Map<LockstepAbortReason, Long> abortedSessions) { }
}
