package com.wordonline.server.game.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.InputRequestDto;
import com.wordonline.server.game.dto.InputResponseDto;
import com.wordonline.server.game.dto.PingChecker;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.MagicInputHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authorization.AuthorizationDeniedException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InputControllerTest {

    @InjectMocks
    private InputController inputController;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private SessionObject sessionObject;

    @Mock
    private PingChecker pingChecker;

    @Mock
    private GameContext gameContext;

    @Mock
    private MagicInputHandler magicInputHandler;

    private PrincipalDetails principalDetails;
    private long userId = 1L;

    @BeforeEach
    void setUp() {
        principalDetails = new PrincipalDetails(userId);
    }

    @Test
    @DisplayName("입력 처리 테스트 - 권한 없음")
    void handleInput_unauthorized() {
        // given
        String sessionId = "test-session";
        long otherUserId = 2L;
        InputRequestDto inputRequestDto = new InputRequestDto();

        // when & then
        assertThatThrownBy(() -> inputController.handleInput(sessionId, otherUserId, inputRequestDto, principalDetails))
                .isInstanceOf(AuthorizationDeniedException.class)
                .hasMessage("Authorization Denied");
    }

    @Test
    @DisplayName("입력 처리 테스트 - 핑")
    void handleInput_ping() {
        // given
        String sessionId = "test-session";
        InputRequestDto inputRequestDto = new InputRequestDto();
        inputRequestDto.setType("ping");

        given(sessionManager.getSessionObject(sessionId)).willReturn(sessionObject);
        given(sessionObject.getPingChecker()).willReturn(pingChecker);

        // when
        inputController.handleInput(sessionId, userId, inputRequestDto, principalDetails);

        // then
        verify(pingChecker).ping(userId);
    }

    @Test
    @DisplayName("입력 처리 테스트 - 기타")
    void handleInput_other() {
        // given
        String sessionId = "test-session";
        InputRequestDto inputRequestDto = new InputRequestDto();
        inputRequestDto.setType("other");
        InputResponseDto responseDto = new InputResponseDto(true, 100, 1, 1L);

        given(sessionManager.getSessionObject(sessionId)).willReturn(sessionObject);
        given(sessionObject.getGameContext()).willReturn(gameContext);
        given(gameContext.getMagicInputHandler()).willReturn(magicInputHandler);
        given(magicInputHandler.handleInput(any(GameContext.class), anyLong(), any(InputRequestDto.class))).willReturn(responseDto);

        // when
        inputController.handleInput(sessionId, userId, inputRequestDto, principalDetails);

        // then
        verify(simpMessagingTemplate).convertAndSend(anyString(), any(InputResponseDto.class));
    }
}
