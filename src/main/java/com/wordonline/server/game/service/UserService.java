package com.wordonline.server.game.service;

import org.springframework.stereotype.Service;

import com.wordonline.server.auth.domain.UserStatus;
import com.wordonline.server.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    /**
     * How much one win against the tutorial opponent moves the player along. Wins rather than
     * matches: losing does not mean they are ready for less help. Four wins from the 0.5 start
     * reach the end, and the opponent gets easier the further behind they are, so a player who
     * keeps losing still gets there.
     */
    static final double NOVICE_PROGRESS_PER_WIN = 0.15;

    private final UserRepository userRepository;

    public void markOnline(long userId) {
        // Skip marking bots (negative user IDs) online
        if (userId < 0) {
            return;
        }
        userRepository.updateStatus(userId, UserStatus.Online);
    }

    public void incrementTotalWins(long userId) {
        userRepository.incrementTotalWins(userId);
    }

    /**
     * How far this player is through the tutorial, from 0.5 to 1.0. Bots and unknown users answer
     * 1.0 - nothing holds back against them.
     */
    public double noviceProgress(long userId) {
        if (userId < 0) {
            return 1.0;
        }
        return userRepository.getNoviceProgress(userId);
    }

    /**
     * Moves a player one win further through the tutorial. The opponent holds back by exactly this
     * number, so every step makes the next match a little less of a gift, and reaching the end is
     * the same thing as leaving the tutorial.
     */
    public void advanceNoviceProgress(long userId) {
        if (userId < 0) {
            return;
        }
        if (userRepository.advanceNoviceProgress(userId, NOVICE_PROGRESS_PER_WIN)) {
            log.info("[User] Advanced novice progress by {}; userId: {}", NOVICE_PROGRESS_PER_WIN, userId);
        }
    }
}
