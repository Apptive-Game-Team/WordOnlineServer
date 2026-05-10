package com.wordonline.server.statistic.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.wordonline.server.auth.repository.UserRepository;
import com.wordonline.server.deck.dto.CardDto;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.system.GameSystem;
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
        gameResultBuilderMap.put(gameContext, builder);
        syncBuilder(gameContext);
    }

    public void saveMagic(GameContext gameContext, long userId, long magicId) {
        getGameResultBuilder(gameContext)
                .ifPresent(builder ->
                        builder.recordMagic(userId, magicId)
                );
    }

    public void syncBuilder(GameContext gameContext) {
        getGameResultBuilder(gameContext).ifPresent(builder -> {
            SessionObject sessionObject = gameContext.getSessionObject();
            builder.setLeftUserId(sessionObject.getLeftUserId());
            builder.setRightUserId(sessionObject.getRightUserId());
            builder.setRunType(sessionObject.getRunType());
            builder.setParameterProfileId(sessionObject.getParameterProfileId());
            builder.setSimulationBatchId(sessionObject.getSimulationBatchId());

            builder.clearCards();
            syncDeck(sessionObject.getLeftUserId(), builder);
            syncDeck(sessionObject.getRightUserId(), builder);
        });
    }

    private void syncDeck(long userId, GameResultBuilder builder) {
        if (userId <= 0) {
            return;
        }

        userRepository.getSelectedDeckId(userId)
                .map(deckService::getDeckCards)
                .ifPresent(cardDtos -> builder.replaceCards(userId, cardDtos));
    }

    public void saveGameResult(GameContext gameContext, Master loser, SessionType sessionType) {
        GameResultBuilder builder = gameResultBuilderMap.remove(gameContext);
        if (builder == null) {
            return;
        }
        statisticRepository.saveGameResultDto(builder.build(loser, sessionType));
    }

    public void saveUpdateTime(GameContext gameContext, Class<? extends GameSystem> clazz, Long intervalNs) {
        getGameResultBuilder(gameContext)
                .ifPresent(builder ->
                        builder.addInterval(clazz, intervalNs)
                );
    }

    private Optional<GameResultBuilder> getGameResultBuilder(GameContext gameContext) {
        return Optional.ofNullable(gameResultBuilderMap.get(gameContext));
    }
}
