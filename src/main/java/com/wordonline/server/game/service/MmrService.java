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

    private final UserRepository userRepository;
    // K‐factor: 신규 유저엔 크게, 숙련자에겐 작게
    private static final double K = 20.0;
    private static final short DEFAULT_MMR = 1000;

    /**
     * 기대 승률(Ea)을 계산
     */
    private double expectedScore(int ratingA, int ratingB) {
        return 1.0 / (1.0 + Math.pow(10.0, (ratingB - ratingA) / 400.0));
    }

    /**
     * 새로운 레이팅 계산
     *
     * @param oldRating 내 기존 레이팅
     * @param opponentRating 상대 레이팅
     * @param score 실제 결과 (Win=1, Draw=0.5, Lose=0)
     */
    private short computeNewRating(short oldRating, short opponentRating, double score) {
        double expected = expectedScore(oldRating, opponentRating);
        double updated = oldRating + K * (score - expected);
        return (short) Math.round(updated);
    }

    @Transactional(readOnly = true)
    public short fetchRating(long userId) {
        // Bots (negative user IDs) have default MMR
        if (userId < 0) {
            return DEFAULT_MMR;
        }
        return userRepository
                .getMmr(userId)                        // Optional<Short>
                .orElse(DEFAULT_MMR);                   // Short
    }
    /**
     * 두 유저의 MMR을 한 번에 업데이트
     *
     * @param userIdA 첫 번째 플레이어 ID
     * @param userIdB 두 번째 플레이어 ID
     * @param outcomeA 첫 번째 플레이어 결과 (WIN/DRAW/LOSE)
     */
    ResultMmrDto updateMatchResult(long userIdA, long userIdB, ResultType outcomeA) {
        // 1) 기존 레이팅 가져오기
        short ratingA = fetchRating(userIdA);
        short ratingB = fetchRating(userIdB);

        // 2) 실제 스코어 계산
        double scoreA = switch (outcomeA) {
            case Win  -> 1.0;
            case Draw -> 0.5;
            case Lose -> 0.0;
        };
        double scoreB = 1.0 - scoreA;

        // 3) 새 레이팅 계산
        short newA = computeNewRating(ratingA, ratingB, scoreA);
        short newB = computeNewRating(ratingB, ratingA, scoreB);

        // 4) DB에 저장
        userRepository.setMmr(userIdA, newA);
        userRepository.setMmr(userIdB, newB);
        log.trace("Updated match result for {}: {}, {}: {}", userIdA, newA, userIdB, newB);

        return new ResultMmrDto(ratingA, ratingB, newA, newB);
    }
}
