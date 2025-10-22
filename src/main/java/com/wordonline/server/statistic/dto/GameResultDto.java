package com.wordonline.server.statistic.dto;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.service.system.GameSystem;
import com.wordonline.server.statistic.domain.UpdateTimeStatistic;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record GameResultDto(
        SessionType sessionType,
        long winUserId,
        long lossUserId,
        Duration duration,
        List<StatisticCardDto> cards,
        List<StatisticMagicDto> magics,
        Map<Class<? extends GameSystem>, UpdateTimeStatistic> updateTimeStatisticMap
) {
    public record StatisticCardDto(
            long cardId,
            long userId,
            int count
    ) {

    }

    @Getter
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
