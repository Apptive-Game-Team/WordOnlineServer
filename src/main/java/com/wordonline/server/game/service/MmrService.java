package com.wordonline.server.game.service;

import com.wordonline.server.auth.repository.UserRepository;
import com.wordonline.server.game.dto.result.ResultMmrDto;
import com.wordonline.server.game.dto.result.ResultType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MmrService {

    private static final double K = 20.0;
    private static final short DEFAULT_MMR = 1000;

    private final UserRepository userRepository;

    private double expectedScore(int ratingA, int ratingB) {
        return 1.0 / (1.0 + Math.pow(10.0, (ratingB - ratingA) / 400.0));
    }

    private short computeNewRating(short oldRating, short opponentRating, double score) {
        double expected = expectedScore(oldRating, opponentRating);
        double updated = oldRating + K * (score - expected);
        return (short) Math.round(updated);
    }

    @Transactional(readOnly = true)
    public short fetchRating(long participantId) {
        return resolveRatingRef(participantId).rating();
    }

    @Transactional(readOnly = true)
    public RatingRef resolveRatingRef(long participantId) {
        short rating = userRepository.getMmr(participantId).orElse(DEFAULT_MMR);
        return new RatingRef(participantId, participantId, participantId < 0, rating);
    }

    ResultMmrDto updateMatchResult(long userIdA, long userIdB, ResultType outcomeA) {
        RatingRef ratingRefA = resolveRatingRef(userIdA);
        RatingRef ratingRefB = resolveRatingRef(userIdB);
        short ratingA = ratingRefA.rating();
        short ratingB = ratingRefB.rating();

        double scoreA = switch (outcomeA) {
            case Win -> 1.0;
            case Draw -> 0.5;
            case Lose -> 0.0;
        };
        double scoreB = 1.0 - scoreA;

        short computedA = computeNewRating(ratingA, ratingB, scoreA);
        short computedB = computeNewRating(ratingB, ratingA, scoreB);
        boolean botMatch = ratingRefA.bot() || ratingRefB.bot();
        short newA = botMatch && !ratingRefA.bot() ? ratingA : computedA;
        short newB = botMatch && !ratingRefB.bot() ? ratingB : computedB;

        if (botMatch) {
            saveBotRating(ratingRefA, newA);
            saveBotRating(ratingRefB, newB);
        } else {
            saveRating(ratingRefA, newA);
            saveRating(ratingRefB, newB);
        }
        log.trace("Updated match result for {}: {}, {}: {}", userIdA, newA, userIdB, newB);

        return new ResultMmrDto(ratingA, ratingB, newA, newB);
    }

    private void saveRating(RatingRef ratingRef, short mmr) {
        userRepository.setMmr(ratingRef.storageId(), mmr);
    }

    private void saveBotRating(RatingRef ratingRef, short mmr) {
        if (ratingRef.bot()) {
            userRepository.setMmr(ratingRef.storageId(), mmr);
        }
    }

    public record RatingRef(long participantId, long storageId, boolean bot, short rating) {
    }
}
