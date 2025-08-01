package com.wordonline.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegisterRequestDto(
        @Email
        String email,
        @NotBlank
        String name,
        @NotBlank
        String passwordPlain) {
}