package com.wordonline.server.auth.service;

import com.wordonline.server.auth.domain.User;
import com.wordonline.server.auth.dto.UserResponseDto;
import com.wordonline.server.auth.repository.UserRepository;
import com.wordonline.server.deck.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DeckService deckService;

    public User registerUser(User user) {
        if (!userRepository.saveUser(user)) {
            throw new AuthorizationDeniedException("Can't Register");
        }

        User actualUser = userRepository.findUserByEmail(user.getEmail())
                .orElseThrow(() -> new AuthorizationDeniedException("Can't Register"));

        long defaultDeckId = deckService.initializeCard(actualUser.getId());
        actualUser.setSelectedDeckId(defaultDeckId);
        return actualUser;
    }

    public UserResponseDto getUser(long userId) {
        return new UserResponseDto(userRepository.findUserById(userId)
                .orElseThrow(() -> new AuthorizationDeniedException("User not found")));
    }

    private User findUserDomain(long userId) {
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new AuthorizationDeniedException("User not found"));
    }

    public void markMatching(long userId) {
        User u = findUserDomain(userId);
        u.markMatching();
        userRepository.updateStatus(userId, u.getStatus());
    }

    public void markPlaying(long userId) {
        User u = findUserDomain(userId);
        u.markPlaying();
        userRepository.updateStatus(userId, u.getStatus());
    }

    public void markOnline(long userId) {
        User u = findUserDomain(userId);
        u.markOnline();
        userRepository.updateStatus(userId, u.getStatus());
    }

    public UserStatus getStatus(long userId) {
        return userRepository.getStatus(userId);
    }
}
