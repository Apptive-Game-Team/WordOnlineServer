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
     * Takes the novice mark off a player who has now met the tutorial opponent, so their next
     * practice match goes to the ordinary bot pool.
     */
    public void clearNovice(long userId) {
        if (userId < 0) {
            return;
        }
        if (userRepository.clearNovice(userId)) {
            log.info("[User] Cleared the novice flag; userId: {}", userId);
        }
    }
}
