package com.wordonline.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @Email
        String email,
        @NotBlank
        String passwordPlain) {
}
