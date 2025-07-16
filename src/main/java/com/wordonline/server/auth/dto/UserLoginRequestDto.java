package com.wordonline.server.auth.dto;

public record UserLoginRequestDto(
        String email,
        String passwordPlain) {
}
