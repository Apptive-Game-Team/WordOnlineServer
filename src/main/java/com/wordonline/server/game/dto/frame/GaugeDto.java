package com.wordonline.server.game.dto.frame;

public record GaugeDto(
        float value,
        float maxValue,
        GaugeCategory category
) {
}

