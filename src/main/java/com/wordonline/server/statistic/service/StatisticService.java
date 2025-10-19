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
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.statistic.domain.GameResultBuilder;
import com.wordonline.server.statistic.repository.StatisticRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticService {

    protected final Map<GameContext, GameResultBuilder> gameResultBuilderMap = new ConcurrentHashMap<>();
    private final StatisticRepository statisticRepository;
    private final DeckService deckService;
    private final UserRepository userRepository;

    public void createBuilder(GameContext gameContext) {
        GameResultBuilder builder = new GameResultBuilder();

        long leftUserId = gameContext.getSessionObject().getLeftUserId();
        long rightUserId = gameContext.getSessionObject().getRightUserId();

        builder.setLeftUserId(leftUserId);
        builder.setRightUserId(rightUserId);

        saveDeck(leftUserId, builder);
        saveDeck(rightUserId, builder);

        gameResultBuilderMap.put(gameContext, builder);
    }

    public void saveMagic(GameContext gameContext, long userId, long magicId) {
        gameResultBuilderMap.get(gameContext)
                .recordMagic(userId, magicId);
    }

    private void saveDeck(long userId, GameResultBuilder builder) {
        long deckId = userRepository.getSelectedDeckId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Deck Not Found"));
        List<CardDto> cardDtos = deckService.getDeck(deckId).cards();
        builder.recordCards(userId, cardDtos);
    }

    public void saveGameResult(GameContext gameContext, Master loser, SessionType sessionType) {
        GameResultBuilder builder = gameResultBuilderMap.remove(gameContext);
        statisticRepository.saveGameResultDto(builder.build(loser, sessionType));
    }
}
