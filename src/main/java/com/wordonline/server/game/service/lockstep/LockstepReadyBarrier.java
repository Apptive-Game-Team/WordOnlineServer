package com.wordonline.server.game.service.lockstep;

import com.wordonline.server.game.dto.lockstep.ClientReadyDto;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

public final class LockstepReadyBarrier {
    private final int protocolVersion;
    private final String simulationVersion;
    private final String configVersion;
    private final Set<Long> participants;
    private final Set<Long> readyParticipants = new LinkedHashSet<>();

    public LockstepReadyBarrier(int protocolVersion, String simulationVersion, String configVersion,
                                Set<Long> participants) {
        this.protocolVersion = protocolVersion;
        this.simulationVersion = simulationVersion;
        this.configVersion = configVersion;
        this.participants = Set.copyOf(participants);
    }

    public synchronized void ready(long userId, ClientReadyDto ready) {
        if (!participants.contains(userId)) {
            throw new IllegalArgumentException("User is not a lockstep participant: " + userId);
        }
        if (ready.protocolVersion() != protocolVersion
                || !simulationVersion.equals(ready.simulationVersion())
                || !configVersion.equals(ready.configVersion())) {
            throw new IllegalArgumentException("Lockstep client version mismatch");
        }
        readyParticipants.add(userId);
        notifyAll();
    }

    public synchronized Set<Long> awaitReady(Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (!readyParticipants.containsAll(participants)) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                break;
            }
            long millis = Math.max(1, remaining / 1_000_000);
            wait(millis);
        }
        Set<Long> missing = new LinkedHashSet<>(participants);
        missing.removeAll(readyParticipants);
        return Set.copyOf(missing);
    }
}
