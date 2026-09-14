package com.wordonline.server.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.repository.MagicParameterRepository;

class MagicParameterServiceTest {

    private static final String SPAWN_HEIGHT = ParameterKey.SPAWN_HEIGHT.dbName();

    private final MagicParameterRepository magicParameterRepository = mock(MagicParameterRepository.class);
    private final MagicParameterService magicParameterService =
            new MagicParameterService(magicParameterRepository);

    @Test
    void aStoredSpawnHeightIsReadFromTheDatabaseOnlyOnce() {
        when(magicParameterRepository.getParameterValue(11L, SPAWN_HEIGHT)).thenReturn(Optional.of(3.0));

        assertThat(magicParameterService.getValueOrDefault(11L, ParameterKey.SPAWN_HEIGHT, 0)).isEqualTo(3.0);
        assertThat(magicParameterService.getValueOrDefault(11L, ParameterKey.SPAWN_HEIGHT, 0)).isEqualTo(3.0);

        verify(magicParameterRepository).getParameterValue(11L, SPAWN_HEIGHT);
        verifyNoMoreInteractions(magicParameterRepository);
    }

    @Test
    void aMissingSpawnHeightFallsBackToTheDefaultAndIsCachedToo() {
        when(magicParameterRepository.getParameterValue(12L, SPAWN_HEIGHT)).thenReturn(Optional.empty());

        assertThat(magicParameterService.findValue(12L, ParameterKey.SPAWN_HEIGHT)).isEmpty();
        assertThat(magicParameterService.getValueOrDefault(12L, ParameterKey.SPAWN_HEIGHT, 10)).isEqualTo(10.0);

        verify(magicParameterRepository).getParameterValue(12L, SPAWN_HEIGHT);
        verifyNoMoreInteractions(magicParameterRepository);
    }

    @Test
    void invalidateCacheClearsMissesToo() {
        when(magicParameterRepository.getParameterValue(13L, SPAWN_HEIGHT)).thenReturn(Optional.empty());
        assertThat(magicParameterService.findValue(13L, ParameterKey.SPAWN_HEIGHT)).isEmpty();

        magicParameterService.invalidateCache();
        when(magicParameterRepository.getParameterValue(13L, SPAWN_HEIGHT)).thenReturn(Optional.of(0.0));

        assertThat(magicParameterService.findValue(13L, ParameterKey.SPAWN_HEIGHT)).contains(0.0);
    }
}
