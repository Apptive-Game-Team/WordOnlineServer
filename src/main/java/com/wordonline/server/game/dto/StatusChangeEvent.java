package com.wordonline.server.game.dto;

public record StatusChangeEvent(
        Status previous,
        Status current
) {
}
