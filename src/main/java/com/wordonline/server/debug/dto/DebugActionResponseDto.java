package com.wordonline.server.debug.dto;

public record DebugActionResponseDto(
        boolean success,
        String message
) {
}
