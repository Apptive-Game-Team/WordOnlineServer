package com.wordonline.server.auth.dto;

import com.wordonline.server.auth.domain.User;

public record UserResponseDto(long id, String email, String name, long selectedDeckId) {

    public UserResponseDto(User user) {
        this(user.getId(), user.getEmail(), user.getName(), user.getSelectedDeckId());
    }
}
