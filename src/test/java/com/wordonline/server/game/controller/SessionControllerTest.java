package com.wordonline.server.game.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordonline.server.auth.config.JwtProvider;
import com.wordonline.server.auth.config.WebSecurityConfig;
import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.dto.UserDetailResponseDto;
import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.dto.frame.SnapshotResponseDto;
import com.wordonline.server.game.service.GameLoop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
@Import(WebSecurityConfig.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionManager sessionManager;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private GameLoop gameLoop;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    private Authentication authentication;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        PrincipalDetails principalDetails = new PrincipalDetails(userId);
        authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
    }

    @Test
    @DisplayName("내 세션 조회 테스트 - 세션 존재")
    void getMySession_found() throws Exception {
        // given
        String sessionId = "test-session";
        long leftUserId = 1L;
        long rightUserId = 2L;
        SessionObject sessionObject = new SessionObject(sessionId, leftUserId, rightUserId, simpMessagingTemplate, Collections.emptyList(), Collections.emptyList(), SessionType.PVP);
        UserDetailResponseDto leftUser = new UserDetailResponseDto(leftUserId, "left-user", "left@example.com");
        UserDetailResponseDto rightUser = new UserDetailResponseDto(rightUserId, "right-user", "right@example.com");

        given(sessionManager.findByUserId(userId)).willReturn(Optional.of(sessionObject));
        given(userService.getUserDetail(leftUserId)).willReturn(leftUser);
        given(userService.getUserDetail(rightUserId)).willReturn(rightUser);

        // when & then
        mockMvc.perform(get("/sessions/mine")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.leftUser.name").value("left-user"))
                .andExpect(jsonPath("$.rightUser.name").value("right-user"));
    }

    @Test
    @DisplayName("내 세션 조회 테스트 - 세션 없음")
    void getMySession_notFound() throws Exception {
        // given
        given(sessionManager.findByUserId(userId)).willReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/sessions/mine")
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("스냅샷 조회 테스트 - 성공")
    void getSnapshot_success() throws Exception {
        // given
        String sessionId = "test-session";
        long leftUserId = 1L;
        long rightUserId = 2L;
        SessionObject sessionObject = new SessionObject(sessionId, leftUserId, rightUserId, simpMessagingTemplate, Collections.emptyList(), Collections.emptyList(), SessionType.PVP);
        SnapshotResponseDto snapshot = new SnapshotResponseDto(0, Collections.emptyList(), Collections.emptyList());

        sessionObject.setGameLoop(gameLoop);
        given(sessionManager.getSessionObject(sessionId)).willReturn(sessionObject);
        given(gameLoop.getLastSnapshot()).willReturn(snapshot);

        // when & then
        mockMvc.perform(get("/sessions/{sessionId}/snapshot", sessionId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("스냅샷 조회 테스트 - 세션 없음")
    void getSnapshot_sessionNotFound() throws Exception {
        // given
        String sessionId = "test-session";
        given(sessionManager.getSessionObject(sessionId)).willReturn(null);

        // when & then
        mockMvc.perform(get("/sessions/{sessionId}/snapshot", sessionId)
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("스냅샷 조회 테스트 - 참가자 아님")
    void getSnapshot_notParticipant() throws Exception {
        // given
        String sessionId = "test-session";
        long leftUserId = 2L;
        long rightUserId = 3L;
        SessionObject sessionObject = new SessionObject(sessionId, leftUserId, rightUserId, simpMessagingTemplate, Collections.emptyList(), Collections.emptyList(), SessionType.PVP);

        given(sessionManager.getSessionObject(sessionId)).willReturn(sessionObject);

        // when & then
        mockMvc.perform(get("/sessions/{sessionId}/snapshot", sessionId)
                        .with(authentication(authentication)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("스냅샷 조회 테스트 - 게임 종료됨")
    void getSnapshot_gameFinished() throws Exception {
        // given
        String sessionId = "test-session";
        long leftUserId = 1L;
        long rightUserId = 2L;
        SessionObject sessionObject = new SessionObject(sessionId, leftUserId, rightUserId, simpMessagingTemplate, Collections.emptyList(), Collections.emptyList(), SessionType.PVP);

        sessionObject.setGameLoop(null);
        given(sessionManager.getSessionObject(sessionId)).willReturn(sessionObject);

        // when & then
        mockMvc.perform(get("/sessions/{sessionId}/snapshot", sessionId)
                        .with(authentication(authentication)))
                .andExpect(status().isGone());
    }
}

