package com.wordonline.server.auth.service;

import java.lang.reflect.Member;

import com.wordonline.server.auth.domain.User;
import com.wordonline.server.auth.domain.UserStatus;
import com.wordonline.server.auth.dto.UserDetailResponseDto;
import com.wordonline.server.auth.dto.UserResponseDto;
import com.wordonline.server.auth.repository.UserRepository;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.matching.client.AccountClient;
import com.wordonline.server.matching.dto.AccountMemberResponseDto;

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
    private final AccountClient accountClient;

    public User initialUser(long memberId) {
        Long userId = userRepository.saveUser(memberId);

        User actualUser = userRepository.findUserById(userId)
                .orElseThrow(() -> new AuthorizationDeniedException("Can't Register"));

        long defaultDeckId = deckService.initializeCard(actualUser.getId());
        actualUser.setSelectedDeckId(defaultDeckId);

        return actualUser;
    }

    public UserResponseDto getUser(long memberId) {
        User user;
        try {
            user = findUserDomain(memberId);
        } catch (AuthorizationDeniedException e) {
            user = initialUser(memberId);
        }
        return new UserResponseDto(user);
    }

    public UserDetailResponseDto getUserDetail(long memberId) {
        AccountMemberResponseDto accountMemberResponseDto = accountClient.getMember(memberId);

        return new UserDetailResponseDto(memberId, accountMemberResponseDto);
    }

    public boolean deleteUser(long userId) {
        return userRepository.deleteById(userId);
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
        return userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId))
                .getStatus();
    }
}
