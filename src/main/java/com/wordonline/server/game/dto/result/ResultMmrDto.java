package com.wordonline.server.game.dto.result;

public record ResultMmrDto(
        short lastLeftMmr,
        short lastRightMmr,
        short newLeftMmr,
        short newRightMmr
) {
}
