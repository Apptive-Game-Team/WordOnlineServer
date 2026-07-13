package com.wordonline.server.game.service.lockstep;

import com.wordonline.server.game.dto.lockstep.ConfirmedInputDto;
import com.wordonline.server.game.dto.lockstep.FrameInputDto;
import com.wordonline.server.game.dto.lockstep.FrameSubmissionDto;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LockstepFrameBuffer {
    private static final Comparator<ConfirmedInputDto> INPUT_ORDER = Comparator
            .comparingLong(ConfirmedInputDto::userId)
            .thenComparingInt(value -> value.input().sequence());

    private final int protocolVersion;
    private final int maximumFutureFrames;
    private final Set<Long> participantIds;
    private final Map<Integer, Map<Long, FrameSubmissionDto>> frames = new HashMap<>();
    private int currentFrame;

    public LockstepFrameBuffer(int protocolVersion, int initialFrame, int maximumFutureFrames, Set<Long> participantIds) {
        if (participantIds.isEmpty()) {
            throw new IllegalArgumentException("Lockstep requires at least one human participant");
        }
        this.protocolVersion = protocolVersion;
        this.currentFrame = initialFrame;
        this.maximumFutureFrames = maximumFutureFrames;
        this.participantIds = Set.copyOf(participantIds);
    }

    public synchronized void submit(long userId, FrameSubmissionDto submission) {
        if (!participantIds.contains(userId)) {
            throw new IllegalArgumentException("User is not a lockstep participant: " + userId);
        }
        if (submission.protocolVersion() != protocolVersion) {
            throw new IllegalArgumentException("Unsupported lockstep protocol: " + submission.protocolVersion());
        }
        if (submission.frameNum() < currentFrame) {
            throw new IllegalArgumentException("Late frame: " + submission.frameNum());
        }
        if (submission.frameNum() > currentFrame + maximumFutureFrames) {
            throw new IllegalArgumentException("Frame exceeds future window: " + submission.frameNum());
        }
        validateSequences(submission.inputs());

        Map<Long, FrameSubmissionDto> submissions = frames.computeIfAbsent(
                submission.frameNum(), ignored -> new HashMap<>());
        FrameSubmissionDto existing = submissions.putIfAbsent(userId, submission);
        if (existing != null && !existing.equals(submission)) {
            throw new IllegalStateException("Conflicting duplicate submission: frame="
                    + submission.frameNum() + ", userId=" + userId);
        }
        notifyAll();
    }

    public synchronized FrameResolution awaitCurrentFrame(Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (!hasAllParticipants(currentFrame)) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                break;
            }
            long millis = Math.max(1, remaining / 1_000_000);
            wait(millis);
        }

        int resolvedFrame = currentFrame;
        Map<Long, FrameSubmissionDto> submissions = frames.remove(resolvedFrame);
        if (submissions == null) {
            submissions = Map.of();
        }

        Set<Long> missing = new LinkedHashSet<>(participantIds);
        missing.removeAll(submissions.keySet());
        List<ConfirmedInputDto> inputs = new ArrayList<>();
        Map<Long, String> hashes = new LinkedHashMap<>();
        submissions.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            hashes.put(entry.getKey(), entry.getValue().previousFrameHash());
            for (FrameInputDto input : entry.getValue().inputs()) {
                inputs.add(new ConfirmedInputDto(entry.getKey(), input));
            }
        });
        inputs.sort(INPUT_ORDER);
        boolean hashMatched = hashes.values().stream().distinct().limit(2).count() <= 1;
        currentFrame++;
        frames.keySet().removeIf(frame -> frame < currentFrame);
        return new FrameResolution(resolvedFrame, inputs, hashes, missing, hashMatched);
    }

    public synchronized int currentFrame() {
        return currentFrame;
    }

    private boolean hasAllParticipants(int frameNum) {
        Map<Long, FrameSubmissionDto> submissions = frames.get(frameNum);
        return submissions != null && submissions.keySet().containsAll(participantIds);
    }

    private static void validateSequences(List<FrameInputDto> inputs) {
        Set<Integer> sequences = new LinkedHashSet<>();
        for (FrameInputDto input : inputs) {
            if (!sequences.add(input.sequence())) {
                throw new IllegalArgumentException("Duplicate input sequence: " + input.sequence());
            }
        }
    }
}
