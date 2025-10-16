package com.wordonline.server.statistic.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.wordonline.server.auth.repository.UserRepository;
import com.wordonline.server.deck.dto.CardDto;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.statistic.domain.GameResultBuilder;
import com.wordonline.server.statistic.repository.StatisticRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final Map<GameLoop, GameResultBuilder> gameResultBuilderMap = new ConcurrentHashMap<>();
    private final StatisticRepository statisticRepository;
    private final DeckService deckService;
    private final UserRepository userRepository;

    public void createBuilder(GameLoop gameLoop) {
        GameResultBuilder builder = new GameResultBuilder();

        long leftUserId = gameLoop.sessionObject.getLeftUserId();
        long rightUserId = gameLoop.sessionObject.getRightUserId();

        builder.setLeftUserId(leftUserId);
        builder.setRightUserId(rightUserId);

        saveDeck(leftUserId, builder);
        saveDeck(rightUserId, builder);

        gameResultBuilderMap.put(gameLoop, builder);
    }

    public void saveMagic(GameLoop gameLoop, long userId, long magicId) {
        gameResultBuilderMap.get(gameLoop)
                .recordMagic(userId, magicId);
    }

    private void saveDeck(long userId, GameResultBuilder builder) {
        long deckId = userRepository.getSelectedDeckId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Deck Not Found"));
        List<CardDto> cardDtos = deckService.getDeck(deckId).cards();
        builder.recordCards(userId, cardDtos);
    }

    public void saveGameResult(GameLoop gameLoop, Master loser, SessionType sessionType) {
        GameResultBuilder builder = gameResultBuilderMap.remove(gameLoop);
        statisticRepository.saveGameResultDto(builder.build(loser, sessionType));
    }
}
