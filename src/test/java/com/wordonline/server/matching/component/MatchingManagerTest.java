package com.wordonline.server.matching.component;

import com.wordonline.server.auth.dto.UserResponseDto;
import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.game.service.ResultChecker;
import com.wordonline.server.matching.dto.MatchedInfoDto;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.statistic.service.StatisticService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.ReflectionTestUtils;

import com.wordonline.server.game.service.GameLoop;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class MatchingManagerTest {

    @MockitoSpyBean
    private MatchingManager matchingManager;

    @Autowired
    private SessionManager sessionManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockitoBean
    private Parameters parameters;

    @MockitoBean
    private StatisticService statisticService;

    private Queue<Long> matchingQueue;

    @BeforeEach
    void setUp() {
        // MatchingManager 내부의 큐를 테스트마다 초기화
        matchingQueue = new LinkedList<>();
        ReflectionTestUtils.setField(matchingManager, "matchingQueue", matchingQueue);
        ReflectionTestUtils.setField(matchingManager, "sessionIdCounter", 0);
    }

    @AfterEach
    void tearDown() {
        // 기존 세션 종료 로직
        Map<String, SessionObject> sessions = (Map<String, SessionObject>) ReflectionTestUtils.getField(sessionManager, "sessions");
        if (sessions != null) {
            sessions.clear(); // 세션 완전 제거
        }

        // matchingManager 상태 초기화
        ReflectionTestUtils.setField(matchingManager, "matchingQueue", new LinkedList<>());
        ReflectionTestUtils.setField(matchingManager, "sessionIdCounter", 0);

        // Mockito Spy 호출 기록 초기화
        clearInvocations(matchingManager);
    }

    @Test
    @DisplayName("유저가 매칭 큐에 성공적으로 등록된다.")
    void enqueue_success() {
        // given
        long userId = 1L;
        doNothing().when(userService).markMatching(userId);
        given(deckService.hasSelectedDeck(userId)).willReturn(true);

        // when
        boolean result = matchingManager.enqueue(userId);

        // then
        assertThat(result).isTrue();
        assertThat(matchingQueue).contains(userId);
    }

    @Test
    @DisplayName("이미 매칭 중인 유저는 큐에 등록되지 않는다.")
    void enqueue_fail_whenAlreadyMatching() {
        // given
        long userId = 1L;
        doThrow(new IllegalStateException("User is already matching")).when(userService).markMatching(userId);

        // when
        boolean result = matchingManager.enqueue(userId);

        // then
        assertThat(result).isFalse();
        assertThat(matchingQueue).doesNotContain(userId);
        verify(userService, times(1)).markOnline(userId);
    }

    @Test
    @DisplayName("선택한 덱이 없는 유저는 큐에 등록되지 않는다.")
    void enqueue_fail_whenNoDeckSelected() {
        // given
        long userId = 1L;
        doNothing().when(userService).markMatching(userId);
        given(deckService.hasSelectedDeck(userId)).willReturn(false);

        // when
        boolean result = matchingManager.enqueue(userId);

        // then
        assertThat(result).isFalse();
        assertThat(matchingQueue).doesNotContain(userId);
        verify(userService, times(1)).markOnline(userId);
    }

    @Test
    @DisplayName("큐에 2명 이상 있을 때 매칭에 성공한다.")
    void tryMatchUsers_success() throws InterruptedException {
        // given
        long user1Id = 1L;
        long user2Id = 2L;

        matchingQueue.add(user1Id);
        matchingQueue.add(user2Id);

        given(userService.getUser(user1Id)).willReturn(new UserResponseDto(user1Id, -1));
        given(userService.getUser(user2Id)).willReturn(new UserResponseDto(user2Id, -1));
        doNothing().when(userService).markPlaying(anyLong());
        given(parameters.getValue("game", "count")).willReturn(10.0); // 세션 수 제한에 걸리지 않도록 설정

        // when
        boolean result = matchingManager.tryMatchUsers();

        // then
        assertThat(result).isTrue();
        assertThat(matchingQueue).isEmpty();
        verify(userService, times(2)).markPlaying(anyLong());
        verify(simpMessagingTemplate, times(2)).convertAndSend(anyString(), any(MatchedInfoDto.class));
        
        // 세션 생성은 비동기로 처리될 수 있으므로 잠시 대기
        Thread.sleep(100);
        assertThat(sessionManager.getActiveSessions()).isEqualTo(1);
    }

    @Test
    @DisplayName("큐에 유저가 1명만 있으면 매칭되지 않는다.")
    void tryMatchUsers_fail_notEnoughUsers() {
        // given
        matchingQueue.add(1L);

        // when
        boolean result = matchingManager.tryMatchUsers();

        // then
        assertThat(result).isFalse();
        assertThat(matchingQueue).hasSize(1);
        verify(simpMessagingTemplate, never()).convertAndSend(anyString(), any(MatchedInfoDto.class));
    }

    @Test
    @DisplayName("게임 세션이 종료되면, 대기열에 유저가 있을 경우 새로운 매칭을 시도한다.")
    void tryMatchUsers_isTriggered_whenSessionEnds() {
        // given
        // 1. 세션이 꽉 찬 상태로 설정
        given(parameters.getValue("game", "count")).willReturn(1.0);
        SessionObject mockSession = new SessionObject("session-to-terminate", 100L, 200L, simpMessagingTemplate, java.util.Collections.emptyList(), java.util.Collections.emptyList());
        GameLoop mockGameLoop = mock(GameLoop.class);
        ResultChecker mockResultChecker = mock(ResultChecker.class);
        when(mockGameLoop.is_running()).thenReturn(true);
        mockGameLoop.resultChecker = mockResultChecker;
        mockSession.setGameLoop(mockGameLoop);

        // SessionManager.createSession() 대신 수동으로 세션 추가 (실제 스레드 생성 방지)
        sessionManager.clearSessions();
        Map<String, SessionObject> sessions = (Map<String, SessionObject>) ReflectionTestUtils.getField(sessionManager, "sessions");
        sessions.put(mockSession.getSessionId(), mockSession);
        assertThat(sessionManager.getActiveSessions()).isEqualTo(1);


        // 2. 매칭 대기열에 2명 추가 및 Mock 설정
        long user1Id = 1L;
        long user2Id = 2L;
        doNothing().when(userService).markMatching(anyLong());
        given(deckService.hasSelectedDeck(anyLong())).willReturn(true);
        matchingManager.enqueue(user1Id);
        matchingManager.enqueue(user2Id);

        given(userService.getUser(user1Id)).willReturn(new UserResponseDto(user1Id, -1));
        given(userService.getUser(user2Id)).willReturn(new UserResponseDto(user2Id, -1));
        doNothing().when(userService).markPlaying(anyLong());

        // 3. tryMatchUsers가 처음 호출되면 실패하도록 설정 (세션이 꽉 찼으므로)
        assertThat(matchingManager.tryMatchUsers()).isFalse();

        verify(matchingManager, times(1)).tryMatchUsers();
        // when
        // 4. 게임 세션 종료를 시뮬레이션
        ReflectionTestUtils.invokeMethod(sessionManager, "onLoopTerminated", mockSession);

        // then
        // 5. onNext 핸들러가 호출되고, 결과적으로 tryMatchUsers가 다시 호출되는지 검증
        // Thread.sleep() 대신 Awaitility를 사용하여 안정적으로 비동기 호출을 기다림
        org.awaitility.Awaitility.await().atMost(20, java.util.concurrent.TimeUnit.SECONDS).untilAsserted(() -> {
            verify(matchingManager, times(2)).tryMatchUsers();
        });
    }
}
