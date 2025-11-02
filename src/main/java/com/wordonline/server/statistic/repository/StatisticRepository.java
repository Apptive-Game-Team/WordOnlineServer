package com.wordonline.server.statistic.repository;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.service.system.GameSystem;
import com.wordonline.server.statistic.domain.UpdateTimeStatistic;
import com.wordonline.server.statistic.dto.GameResultDto;
import com.wordonline.server.statistic.dto.GameResultDto.StatisticCardDto;
import com.wordonline.server.statistic.dto.GameResultDto.StatisticMagicDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StatisticRepository {

    private final JdbcClient jdbcClient;

    private final static String SAVE_GAME_RESULT = """
            INSERT INTO statistic_games(win_user_id, loss_user_id, duration, game_type)
            VALUES(:winUserId, :lossUserId, :duration, :gameType::game_type) RETURNING id;
            """;
    private final static String SAVE_CARD = """
            INSERT INTO statistic_game_cards(user_id, statistic_game_id, card_id, count)
            VALUES(:userId, :gameId, :cardId, :count);
            """;
    private final static String SAVE_MAGIC = """
            INSERT INTO statistic_game_magics(user_id, statistic_game_id, magic_id, count)
            VALUES(:userId, :gameId, :magicId, :count);
            """;

    private final static String SAVE_UPDATE_TIME = """
            INSERT INTO statistic_update_time(statistic_game_id, name, min_interval_ns, max_interval_ns, mean_interval_ns)
            VALUES(:gameId, :name, :minInterval, :maxInterval, :meanInterval);
            """;

    public void saveGameResultDto(GameResultDto gameResultDto) {
        long gameId = saveGame(gameResultDto.sessionType(), gameResultDto.winUserId(), gameResultDto.lossUserId(), gameResultDto.duration());
        saveCard(gameId, gameResultDto.cards());
        saveMagic(gameId, gameResultDto.magics());
        saveUpdateTime(gameId, gameResultDto.updateTimeStatisticMap());
    }


    private void saveUpdateTime(long gameId, Map<Class<? extends GameSystem>, UpdateTimeStatistic> updateTimeStatisticMap) {
        updateTimeStatisticMap.forEach((key, value) -> jdbcClient.sql(SAVE_UPDATE_TIME)
                .param("gameId", gameId)
                .param("minInterval", value.getMinInterval())
                .param("maxInterval", value.getMaxInterval())
                .param("meanInterval", value.getMeanInterval())
                .param("name", key.getSimpleName())
                .update());
    }

    private long saveGame(SessionType sessionType, long winUserId, long lossUserId, Duration duration) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(SAVE_GAME_RESULT)
                .param("gameType", sessionType.name())
                .param("winUserId", winUserId)
                .param("lossUserId", lossUserId)
                .param("duration", duration.toSeconds())
                .update(keyHolder);
        return keyHolder.getKey().longValue();
    }

    private void saveCard(long gameId, List<StatisticCardDto> cardDtos) {
        cardDtos.forEach(cardDto -> {
            jdbcClient.sql(SAVE_CARD)
                    .param("userId", cardDto.userId())
                    .param("gameId", gameId)
                    .param("cardId", cardDto.cardId())
                    .param("count", cardDto.count())
                    .update();
        });
    }

    private void saveMagic(long gameId, List<StatisticMagicDto> magicDtos) {
        magicDtos.forEach(magicDto -> {
            jdbcClient.sql(SAVE_MAGIC)
                    .param("userId", magicDto.getUserId())
                    .param("gameId", gameId)
                    .param("magicId", magicDto.getMagicId())
                    .param("count", magicDto.getCount())
                    .update();
        });
    }
}
