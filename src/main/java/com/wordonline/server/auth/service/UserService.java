package com.wordonline.server.auth.service;

import com.wordonline.server.auth.domain.KakaoUser;
import com.wordonline.server.auth.repository.UserRepository;
import com.wordonline.server.deck.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DeckService deckService;

    public void registerUser(KakaoUser kakaoUser) throws AuthenticationException {
        if (!userRepository.saveUser(kakaoUser)) {
            throw new AuthenticationException("Can't Register");
        }

        deckService.initializeCard(kakaoUser.id());
    }

    public void loginOrRegisterUser(KakaoUser kakaoUser) throws AuthenticationException {
        Optional<KakaoUser> userOptional = userRepository.findUserById(kakaoUser.id());
        if (userOptional.isEmpty()) {
            registerUser(kakaoUser);
            return;
        }

        KakaoUser user = userOptional.get();

        if (user.email().equals(kakaoUser.email()) &&
                user.nickname().equals(kakaoUser.nickname())) {
            return;
        }

        throw new AuthenticationException("Can't Login");
    }

    public KakaoUser getUser(long userId) {
        return userRepository.findUserById(userId).get();
    }
}
