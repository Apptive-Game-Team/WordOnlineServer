package com.wordonline.server.game.service;

import com.wordonline.server.auth.repository.UserRepository;
import com.wordonline.server.game.dto.result.ResultType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MmrServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final MmrService service = new MmrService(userRepository);

    @Test
    void resolvesBotRatingFromNegativeUser() {
        when(userRepository.getMmr(-7)).thenReturn(Optional.of((short) 1200));

        MmrService.RatingRef result = service.resolveRatingRef(-7);

        assertThat(result.rating()).isEqualTo((short) 1200);
        assertThat(result.storageId()).isEqualTo(-7);
        assertThat(result.bot()).isTrue();
    }

    @Test
    void botMatchUpdatesOnlyBotUserRating() {
        when(userRepository.getMmr(10)).thenReturn(Optional.of((short) 1000));
        when(userRepository.getMmr(-7)).thenReturn(Optional.of((short) 1000));

        service.updateMatchResult(10, -7, ResultType.Win);

        verify(userRepository, never()).setMmr(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.anyShort());
        verify(userRepository).setMmr(org.mockito.ArgumentMatchers.eq(-7L), org.mockito.ArgumentMatchers.anyShort());
    }
}
