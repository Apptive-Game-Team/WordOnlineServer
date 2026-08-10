package com.wordonline.server.game.controller;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.PingChecker;
import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.service.LocalizationService;
import com.wordonline.server.session.service.SessionService;

class InputControllerTest {

    private final SessionService sessionService = mock(SessionService.class);
    private final SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
    private final LocalizationService localizationService = mock(LocalizationService.class);
    private final SessionObject sessionObject = mock(SessionObject.class);
    private final PingChecker pingChecker = mock(PingChecker.class);
    private final InputController controller = new InputController();

    InputControllerTest() {
        ReflectionTestUtils.setField(controller, "sessionService", sessionService);
        ReflectionTestUtils.setField(controller, "template", template);
        ReflectionTestUtils.setField(controller, "localizationService", localizationService);
        when(sessionService.getSessionObject("room-5")).thenReturn(sessionObject);
    }

    private static InputRequestDto pingRequest() {
        InputRequestDto dto = new InputRequestDto();
        dto.setType("ping");
        return dto;
    }

    private static PrincipalDetails principal(long uid) {
        PrincipalDetails principalDetails = mock(PrincipalDetails.class);
        when(principalDetails.getUid()).thenReturn(uid);
        return principalDetails;
    }

    @Test
    void rejectsInputFromUserWhoIsNotInTheSession() {
        when(sessionObject.getUserSide(77L)).thenReturn(null);

        assertThatThrownBy(() -> controller.handleInput("room-5", 77L, pingRequest(), principal(77L)))
                .isInstanceOf(AuthorizationDeniedException.class);

        verifyNoInteractions(pingChecker);
    }

    @Test
    void acceptsInputFromSessionParticipant() {
        when(sessionObject.getUserSide(7L)).thenReturn(Master.LeftPlayer);
        when(sessionObject.getPingChecker()).thenReturn(pingChecker);

        assertThatCode(() -> controller.handleInput("room-5", 7L, pingRequest(), principal(7L)))
                .doesNotThrowAnyException();

        verify(pingChecker).ping(7L);
    }
}
