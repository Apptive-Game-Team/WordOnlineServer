package com.wordonline.server.matching.component;

import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@ActiveProfiles("test")
@SpringBootTest
public class MatchingManagerSessionManagerIntegrationTest {

    @Autowired
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

    @MockitoBean
    private DatabaseMagicParser magicParser;

    @BeforeEach
    void setUp() {
        // MatchingManager와 SessionManager의 상태를 초기화합니다.
        Queue<Long> matchingQueue = (Queue<Long>) ReflectionTestUtils.getField(matchingManager, "matchingQueue");
        if (matchingQueue != null) {
            matchingQueue.clear();
        }
        sessionManager.clearSessions();
        ReflectionTestUtils.setField(matchingManager, "sessionIdCounter", 0);

        // Mock 객체의 기본 동작을 설정합니다.
        given(deckService.hasSelectedDeck(anyLong())).willReturn(true);
        doNothing().when(userService).markMatching(anyLong());
        doNothing().when(userService).markPlaying(anyLong());
        doNothing().when(userService).markOnline(anyLong());
    }

    @AfterEach
    void tearDown() {
        sessionManager.clearSessions();
    }

    @Test
    @DisplayName("세션이 종료되고 대기열에 2명 이상 있으면, 자동으로 새로운 매칭이 성사된다.")
    void whenSessionEnds_andQueueHasUsers_thenNewMatchIsMade() {
        // given
        // 1. 최대 세션 수를 1로 제한
        given(parameters.getValue("game", "count")).willReturn(1.0);

        // 2. 첫 번째 매칭을 위한 유저(1, 2)를 대기열에 추가하고 매칭시킨다.
        matchingManager.enqueue(1L);
        matchingManager.enqueue(2L);
        assertThat(matchingManager.tryMatchUsers()).isTrue();

        // 3. 세션이 생성될 때까지 대기 (최대 5초)
        await().atMost(5, TimeUnit.SECONDS).until(() -> sessionManager.getActiveSessions() == 1);
        assertThat(matchingManager.getQueueLength()).isZero();

        // 4. 새로운 유저(3, 4)를 대기열에 추가한다.
        matchingManager.enqueue(3L);
        matchingManager.enqueue(4L);
        assertThat(matchingManager.getQueueLength()).isEqualTo(2);

        // 5. 세션이 꽉 찼으므로 매칭은 실패해야 한다.
        assertThat(matchingManager.tryMatchUsers()).isFalse();
        assertThat(matchingManager.getQueueLength()).isEqualTo(2);

        // when
        // 6. 첫 번째 세션을 종료시킨다.
        Optional<SessionObject> sessionToTerminate = sessionManager.findByUserId(1L);
        assertThat(sessionToTerminate).isPresent();
        // onLoopTerminated는 내부적으로 numOfSessionsFlow.submit()을 호출하여 onNext를 트리거한다.
        ReflectionTestUtils.invokeMethod(sessionManager, "onLoopTerminated", sessionToTerminate.get());

        // then
        // 7. 대기열에 있던 유저(3, 4)로 새로운 세션이 자동으로 생성되는지 확인
        await().atMost(5, TimeUnit.SECONDS).until(() -> sessionManager.getActiveSessions() == 1);
        
        // 8. 새로운 세션이 유저 3과 4를 포함하는지 확인
        Optional<SessionObject> newSession = sessionManager.findByUserId(3L);
        assertThat(newSession).isPresent();
        assertThat(newSession.get().getLeftUserId()).isEqualTo(3L);
        assertThat(newSession.get().getRightUserId()).isEqualTo(4L);

        // 9. 대기열은 비워져야 한다.
        assertThat(matchingManager.getQueueLength()).isZero();
    }
}
