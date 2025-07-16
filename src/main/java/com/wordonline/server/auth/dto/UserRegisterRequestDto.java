package com.wordonline.server.auth.dto;

public record UserRegisterRequestDto(
        String email,
        String name,
        String passwordPlain) {
}