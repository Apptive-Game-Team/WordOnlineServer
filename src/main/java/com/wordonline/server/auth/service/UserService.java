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

        deckService.initializeCard(actualUser.getId());

        return actualUser;
    }

    public UserResponseDto getUser(long userId) {
        return new UserResponseDto(userRepository.findUserById(userId)
                .orElseThrow(() -> new AuthorizationDeniedException("User not found")));
    }
}
