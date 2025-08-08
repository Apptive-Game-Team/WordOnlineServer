package com.wordonline.server.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Getter
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String name;
    private String passwordHash;
    private UserStatus status;

    public static User createWithPasswordPlain(String email, String name, String password) {
        return new User(null, email, name, BCrypt.hashpw(password, BCrypt.gensalt()), UserStatus.Online);
    }

    public boolean validatePassword(String password) {
        return BCrypt.checkpw(password, passwordHash);
    }
    // 상태 전환 편의 메서드
    public void markMatching() {
        if (status != UserStatus.Online)
            throw new IllegalStateException("잘못된 상태에서 매칭 시도");
        this.status = UserStatus.OnMatching;
    }

    public void markPlaying() {
        if (status != UserStatus.OnMatching)
            throw new IllegalStateException("잘못된 상태에서 플레이 시작");
        this.status = UserStatus.OnPlaying;
    }

    public void markOnline() {
        this.status = UserStatus.Online;
    }
}
