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

    public static User createWithPasswordPlain(String email, String name, String password) {
        return new User(null, email, name, BCrypt.hashpw(password, BCrypt.gensalt()));
    }

    public boolean validatePassword(String password) {
        return BCrypt.checkpw(password, passwordHash);
    }
}
