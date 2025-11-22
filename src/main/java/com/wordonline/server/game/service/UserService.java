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
        userRepository.updateStatus(userId, UserStatus.Online);
    }
}
