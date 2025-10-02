package com.wordonline.server.auth.domain;

import com.wordonline.server.deck.service.DeckService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Getter
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private UserStatus status;
    @Setter
    private Long selectedDeckId;

    // 상태 전환 편의 메서드
    public void markMatching() {
        this.status = UserStatus.OnMatching;
    }

    public void markPlaying() {
        this.status = UserStatus.OnPlaying;
    }

    public void markOnline() {
        this.status = UserStatus.Online;
    }

    public User(String name) {
        this(null, name, UserStatus.Online, null);
    }
}
