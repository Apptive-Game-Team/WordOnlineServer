package com.wordonline.server.statistic.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.wordonline.server.deck.dto.CardDto;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.statistic.dto.GameResultDto;
import com.wordonline.server.statistic.dto.GameResultDto.StatisticCardDto;
import com.wordonline.server.statistic.dto.GameResultDto.StatisticMagicDto;

import lombok.Setter;

@Setter
public class GameResultBuilder {

    private long leftUserId;
    private long rightUserId;
    private final List<StatisticCardDto> cardDtos = new ArrayList<>();
    private final List<StatisticMagicDto> magicDtos = new ArrayList<>();

    private final LocalDateTime startTime = LocalDateTime.now();

    public void recordCards(long userId, List<CardDto> cardDtos) {
        Map<Long, Long> counts = cardDtos.stream()
                .collect(Collectors.groupingBy(CardDto::id, Collectors.counting()));

        for (Map.Entry<Long, Long> entry : counts.entrySet()) {
            long cardId = entry.getKey();
            int count = entry.getValue().intValue();

            Optional<StatisticCardDto> existing = this.cardDtos.stream()
                    .filter(c -> c.cardId() == cardId && c.userId() == userId)
                    .findFirst();

            if (existing.isPresent()) {
                StatisticCardDto old = existing.get();
                this.cardDtos.remove(old);
                this.cardDtos.add(new StatisticCardDto(cardId, userId, old.count() + count));
            } else {
                this.cardDtos.add(new StatisticCardDto(cardId, userId, count));
            }
        }
    }

    public void recordMagic(long userId, long magicId) {
        magicDtos.stream()
                .filter(magicDto -> magicDto.belongsTo(userId, magicId))
                .findAny()
                .ifPresentOrElse(
                        StatisticMagicDto::add,
                        () -> magicDtos.add(new StatisticMagicDto(userId, magicId))
                );
    }

    public GameResultDto build(Master loser) {

        Duration duration = Duration.between(startTime, LocalDateTime.now());

        long winId;
        long lossId;
        if (loser == Master.RightPlayer) {
            winId = leftUserId;
            lossId = rightUserId;
        } else if (loser == Master.LeftPlayer) {
            winId = rightUserId;
            lossId = leftUserId;
        } else {
            return null;
        }

        return new GameResultDto(
                winId,
                lossId,
                duration,
                cardDtos,
                magicDtos
        );
    }
}
