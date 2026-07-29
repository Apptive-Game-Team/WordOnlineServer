package com.wordonline.server.statistic.service;

import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.statistic.domain.GameResultBuilder;
import com.wordonline.server.statistic.repository.StatisticRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class StatisticServiceTest {

    @Test
    void dropsBuilderWithoutSavingWhenMatchHasNoLoser() {
        StatisticRepository repository = mock(StatisticRepository.class);
        StatisticService service = new StatisticService(repository, mock(DeckService.class));
        GameContext gameContext = mock(GameContext.class);
        service.gameResultBuilderMap.put(gameContext, new GameResultBuilder());

        service.saveGameResult(gameContext, null, SessionType.PVP);

        assertThat(service.gameResultBuilderMap).isEmpty();
        verifyNoInteractions(repository);
    }
}
