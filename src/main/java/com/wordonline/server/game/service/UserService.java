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
}
