package com.wordonline.server.game.service;

import org.springframework.stereotype.Service;

import com.wordonline.server.auth.domain.UserStatus;
import com.wordonline.server.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

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
     * How far this player is through the tutorial, from 0.5 to 1.0. Bots and unknown users answer
     * 1.0 - nothing holds back against them.
     */
    public double noviceProgress(long userId) {
        if (userId < 0) {
            return 1.0;
        }
        return userRepository.getNoviceProgress(userId);
    }
}
