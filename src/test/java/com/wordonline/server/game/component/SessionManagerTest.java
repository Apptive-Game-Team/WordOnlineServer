package com.wordonline.server.game.component;

import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.MmrService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
class SessionManagerTest {

    @Autowired
    private SessionManager sessionManager;

    // SessionManager가 의존하는 빈들을 Mock으로 생성
    @MockitoBean
    private MmrService mmrService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @AfterEach
    void tearDown() {
        // 각 테스트 후 세션 맵을 비웁니다.
        Map<String, SessionObject> sessions = (Map<String, SessionObject>) ReflectionTestUtils.getField(sessionManager, "sessions");
        if (sessions != null) {
            // 남아있는 모든 세션의 게임 루프를 종료시킵니다.
            for (SessionObject session : sessions.values()) {
                if (session.getGameLoop() != null && session.getGameLoop().is_running()) {
                    ReflectionTestUtils.invokeMethod(sessionManager, "onLoopTerminated", session);
                }
            }
            sessions.clear();
        }
    }

    @Test
    @DisplayName("세션을 성공적으로 생성하고 조회한다.")
    void createAndGetSession() throws InterruptedException {
        // given
        String sessionId = "test-session-1";
        SessionObject sessionObject = new SessionObject(sessionId, 1L, 2L, simpMessagingTemplate, Collections.emptyList(), Collections.emptyList());

        // when
        sessionManager.createSession(sessionObject);
        // GameLoop 스레드가 시작될 시간을 잠시 줍니다.
        Thread.sleep(100);

        // then
        SessionObject retrievedSession = sessionManager.getSessionObject(sessionId);
        assertThat(retrievedSession).isNotNull();
        assertThat(retrievedSession.getSessionId()).isEqualTo(sessionId);
        assertThat(sessionManager.getActiveSessions()).isEqualTo(1);
    }

    @Test
    @DisplayName("유저 ID로 세션을 찾을 수 있다.")
    void findByUserId() throws InterruptedException {
        // given
        String sessionId = "test-session-2";
        long user1Id = 10L;
        long user2Id = 20L;
        SessionObject sessionObject = new SessionObject(sessionId, user1Id, user2Id, simpMessagingTemplate, Collections.emptyList(), Collections.emptyList());
        sessionManager.createSession(sessionObject);
        Thread.sleep(100);

        // when
        Optional<SessionObject> foundByLeftUser = sessionManager.findByUserId(user1Id);
        Optional<SessionObject> foundByRightUser = sessionManager.findByUserId(user2Id);
        Optional<SessionObject> notFoundUser = sessionManager.findByUserId(99L);

        // then
        assertThat(foundByLeftUser).isPresent();
        assertThat(foundByLeftUser.get().getSessionId()).isEqualTo(sessionId);

        assertThat(foundByRightUser).isPresent();
        assertThat(foundByRightUser.get().getSessionId()).isEqualTo(sessionId);

        assertThat(notFoundUser).isNotPresent();
    }

    @Test
    @DisplayName("게임 루프가 종료되면 세션이 제거된다.")
    void onLoopTerminated() {
        // given
        String sessionId = "test-session-3";
        SessionObject sessionObject = new SessionObject(sessionId, 1L, 2L, simpMessagingTemplate, Collections.emptyList(), Collections.emptyList());

        // GameLoop를 모킹하여 즉시 종료된 것처럼 만듭니다.
        GameLoop mockGameLoop = mock(GameLoop.class);
        when(mockGameLoop.is_running()).thenReturn(false);
        sessionObject.setGameLoop(mockGameLoop);

        // 세션 맵에 직접 추가
        Map<String, SessionObject> sessions = (Map<String, SessionObject>) ReflectionTestUtils.getField(sessionManager, "sessions");
        sessions.put(sessionId, sessionObject);

        assertThat(sessions).hasSize(1);

        // when
        // private 메서드인 onLoopTerminated를 Reflection을 사용해 호출
        ReflectionTestUtils.invokeMethod(sessionManager, "onLoopTerminated", sessionObject);

        // then
        assertThat(sessions).isEmpty();
        assertThat(sessionManager.getActiveSessions()).isZero();
    }
}
