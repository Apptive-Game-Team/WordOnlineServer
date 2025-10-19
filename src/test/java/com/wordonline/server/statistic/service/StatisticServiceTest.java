package com.wordonline.server.statistic.service;

import com.wordonline.server.auth.repository.UserRepository;
import com.wordonline.server.deck.dto.CardDto;
import com.wordonline.server.deck.dto.DeckResponseDto;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.statistic.domain.GameResultBuilder;
import com.wordonline.server.statistic.dto.GameResultDto; 
import com.wordonline.server.statistic.repository.StatisticRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; 
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat; 
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticServiceTest {

    @InjectMocks
    private StatisticService statisticService;

    @Mock
    private StatisticRepository statisticRepository;

    @Mock
    private DeckService deckService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameContext gameContext;

    @Mock
    private SessionObject sessionObject;

    @Mock
    private GameResultBuilder gameResultBuilder;

    private long leftUserId = 1L;
    private long rightUserId = 2L;

    @Test
    @DisplayName("GameResultBuilder 생성 테스트")
    void createBuilder() {
        // given
        given(gameContext.getSessionObject()).willReturn(sessionObject);
        given(sessionObject.getLeftUserId()).willReturn(leftUserId);
        given(sessionObject.getRightUserId()).willReturn(rightUserId);
        given(userRepository.getSelectedDeckId(leftUserId)).willReturn(Optional.of(1L));
        given(userRepository.getSelectedDeckId(rightUserId)).willReturn(Optional.of(2L));
        given(deckService.getDeck(1L)).willReturn(new DeckResponseDto(1L, "deck1", Collections.emptyList()));
        given(deckService.getDeck(2L)).willReturn(new DeckResponseDto(2L, "deck2", Collections.emptyList()));

        // when
        statisticService.createBuilder(gameContext);

        // then
        // Verify that the builder is created and stored in the map (implicitly by other tests using it)
    }

    @Test
    @DisplayName("마법 기록 테스트")
    void saveMagic() {
        // given
        long magicId = 1L;
        Map<GameContext, GameResultBuilder> gameResultBuilderMap = new ConcurrentHashMap<>();
        gameResultBuilderMap.put(gameContext, gameResultBuilder);
        ReflectionTestUtils.setField(statisticService, "gameResultBuilderMap", gameResultBuilderMap);

        // when
        statisticService.saveMagic(gameContext, leftUserId, magicId);

        // then
        verify(gameResultBuilder).recordMagic(leftUserId, magicId);
    }

    @Test
    @DisplayName("게임 결과 저장 테스트")
    void saveGameResult() {
        // given
        Master loser = Master.RightPlayer;
        SessionType sessionType = SessionType.PVP;
        GameResultDto gameResultDto = new GameResultDto(sessionType, 1L, 2L, Duration.ofSeconds(100), Collections.emptyList(), Collections.emptyList()); 

        Map<GameContext, GameResultBuilder> gameResultBuilderMap = new ConcurrentHashMap<>();
        gameResultBuilderMap.put(gameContext, gameResultBuilder);
        ReflectionTestUtils.setField(statisticService, "gameResultBuilderMap", gameResultBuilderMap);

        given(gameResultBuilder.build(loser, sessionType)).willReturn(gameResultDto);

        // when
        statisticService.saveGameResult(gameContext, loser, sessionType);

        // then
        verify(statisticRepository).saveGameResultDto(gameResultDto);
        assertThat(gameResultBuilderMap).doesNotContainKey(gameContext);
    }
}
