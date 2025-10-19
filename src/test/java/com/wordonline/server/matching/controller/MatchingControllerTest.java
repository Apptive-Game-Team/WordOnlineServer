package com.wordonline.server.matching.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.matching.component.MatchingManager;
import com.wordonline.server.matching.dto.QueueLengthResponseDto;
import com.wordonline.server.matching.dto.SimpleMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MatchingControllerTest {

    @InjectMocks
    private MatchingController matchingController;

    @Mock
    private MatchingManager matchingManager;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Test
    @DisplayName("매칭 큐 길이 조회 테스트")
    void getQueueLength() {
        // given
        given(matchingManager.getQueueLength()).willReturn(5);

        // when
        ResponseEntity<QueueLengthResponseDto> response = matchingController.getQueueLength();

        // then
        assertThat(response.getBody().length()).isEqualTo(5);
    }

    @Test
    @DisplayName("매칭 큐 추가 테스트 - 성공")
    void queueMatching_success() {
        // given
        long userId = 1L;
        PrincipalDetails principalDetails = new PrincipalDetails(userId);
        given(matchingManager.enqueue(userId)).willReturn(true);

        // when
        matchingController.queueMatching(principalDetails);

        // then
        ArgumentCaptor<SimpleMessageDto> messageCaptor = ArgumentCaptor.forClass(SimpleMessageDto.class);
        verify(simpMessagingTemplate).convertAndSend(eq(String.format("/queue/match-status/%s", userId)), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getMessage()).isEqualTo("Successfully Enqueued");
        verify(matchingManager).tryMatchUsers();
    }

    @Test
    @DisplayName("매칭 큐 추가 테스트 - 실패")
    void queueMatching_fail() {
        // given
        long userId = 1L;
        PrincipalDetails principalDetails = new PrincipalDetails(userId);
        given(matchingManager.enqueue(userId)).willReturn(false);

        // when
        matchingController.queueMatching(principalDetails);

        // then
        ArgumentCaptor<SimpleMessageDto> messageCaptor = ArgumentCaptor.forClass(SimpleMessageDto.class);
        verify(simpMessagingTemplate).convertAndSend(eq(String.format("/queue/match-status/%s", userId)), messageCaptor.capture());
        assertThat(messageCaptor.getValue().getMessage()).isEqualTo("Failed to enqueue user");
        verify(matchingManager).tryMatchUsers();
    }
}
