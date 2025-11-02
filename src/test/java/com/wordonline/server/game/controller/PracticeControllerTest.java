package com.wordonline.server.game.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.matching.component.MatchingManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PracticeControllerTest {

    @InjectMocks
    private PracticeController practiceController;

    @Mock
    private MatchingManager matchingManager;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    private PrincipalDetails principalDetails;
    private long userId = 1L;

    @BeforeEach
    void setUp() {
        principalDetails = new PrincipalDetails(userId);
    }

    @Test
    @DisplayName("연습 게임 매칭 요청 테스트")
    void queueMatching() {
        // when
        practiceController.queueMatching(principalDetails);

        // then
        verify(matchingManager).matchPractice(userId);
    }
}
