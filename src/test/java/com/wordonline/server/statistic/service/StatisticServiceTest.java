package com.wordonline.server.statistic.service;

import java.util.Optional;

import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.statistic.domain.GameOutcome;
import com.wordonline.server.statistic.domain.GameResultBuilder;
import com.wordonline.server.statistic.dto.GameResultDto;
import com.wordonline.server.statistic.repository.StatisticRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StatisticServiceTest {

    // A draw used to be dropped on the floor; it is a finished game and must be recorded.
    @Test
    void savesDrawResultWhenMatchHasNoLoser() {
        StatisticRepository repository = mock(StatisticRepository.class);
        when(repository.saveGameResultDto(any())).thenReturn(42L);
        StatisticService service = new StatisticService(repository, mock(DeckService.class));
        GameContext gameContext = mock(GameContext.class);
        service.gameResultBuilderMap.put(gameContext, new GameResultBuilder());

        Optional<Long> statisticGameId = service.saveGameResult(gameContext, null, SessionType.PVP);

        assertThat(service.gameResultBuilderMap).isEmpty();
        assertThat(statisticGameId).contains(42L);
        ArgumentCaptor<GameResultDto> captor = ArgumentCaptor.forClass(GameResultDto.class);
        verify(repository).saveGameResultDto(captor.capture());
        assertThat(captor.getValue().outcome()).isEqualTo(GameOutcome.DRAW);
        assertThat(captor.getValue().winUserId()).isNull();
        assertThat(captor.getValue().lossUserId()).isNull();
    }

    @Test
    void abandonedResultFlushesBuilderWithAbandonedOutcome() {
        StatisticRepository repository = mock(StatisticRepository.class);
        when(repository.saveGameResultDto(any())).thenReturn(7L);
        StatisticService service = new StatisticService(repository, mock(DeckService.class));
        GameContext gameContext = mock(GameContext.class);
        service.gameResultBuilderMap.put(gameContext, new GameResultBuilder());

        Optional<Long> statisticGameId = service.saveAbandonedGameResult(gameContext, SessionType.PVP);

        assertThat(service.gameResultBuilderMap).isEmpty();
        assertThat(statisticGameId).contains(7L);
        ArgumentCaptor<GameResultDto> captor = ArgumentCaptor.forClass(GameResultDto.class);
        verify(repository).saveGameResultDto(captor.capture());
        assertThat(captor.getValue().outcome()).isEqualTo(GameOutcome.ABANDONED);
    }

    // Debug sessions never get a builder; their end must not write statistics.
    @Test
    void returnsEmptyWithoutSavingWhenSessionHasNoBuilder() {
        StatisticRepository repository = mock(StatisticRepository.class);
        StatisticService service = new StatisticService(repository, mock(DeckService.class));

        Optional<Long> statisticGameId =
                service.saveGameResult(mock(GameContext.class), Master.LeftPlayer, SessionType.PVP);

        assertThat(statisticGameId).isEmpty();
        verifyNoInteractions(repository);
    }

    @Test
    void saveGameResultIsTransactionalSoStatisticInsertsCommitTogether() throws Exception {
        Transactional annotation = StatisticService.class
                .getDeclaredMethod("saveGameResult", GameContext.class, Master.class, SessionType.class)
                .getAnnotation(Transactional.class);

        assertThat(annotation).isNotNull();
    }
}
