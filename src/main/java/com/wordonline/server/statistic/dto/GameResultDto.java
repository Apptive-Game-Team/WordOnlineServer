package com.wordonline.server.statistic.dto;

import java.time.Duration;
import java.util.List;

import lombok.AllArgsConstructor;

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

    @AllArgsConstructor
    public static class StatisticMagicDto {
        long magicId;
        long userId;
        int count;

        public StatisticMagicDto(long userId, long magicId) {
            this(magicId, userId, 1);
        }

        public boolean belongsTo(long userId, long magicId) {
            return this.userId == userId && this.magicId == magicId;
        }

        public void add() {
            count++;
        }
    }
}
