package com.wordonline.server.statistic.dto;

import java.time.Duration;
import java.util.List;

public record GameResultDto(
        long winUserId,
        long lossUserId,
        Duration duration,
        List<StatisticCardDto> cards,
        List<StatisticMagicDto> magics
) {
    public record StatisticCardDto(
            long cardId,
            long userId,
            int count
    ) {

    }

    public record StatisticMagicDto(
            long magicId,
            long userId,
            int count
    ) {

    }
}
