package com.wordonline.server.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.wordonline.server.game.repository.ParameterRepository;

class ParameterServiceTest {

    private final ParameterRepository parameterRepository = mock(ParameterRepository.class);
    private final ParameterService parameterService = new ParameterService(parameterRepository);

    @Test
    void aMissingParameterIsReadFromTheDatabaseOnlyOnce() {
        when(parameterRepository.getParameterValue("zap_mouse", "radius")).thenReturn(Optional.empty());

        assertThat(parameterService.getValueOrDefault("zap_mouse", "radius", 2.5)).isEqualTo(2.5);
        assertThat(parameterService.getValueOrDefault("zap_mouse", "radius", 2.5)).isEqualTo(2.5);
        assertThat(parameterService.getValueOrDefault("zap_mouse", "radius", 7.0)).isEqualTo(7.0);

        verify(parameterRepository).getParameterValue("zap_mouse", "radius");
        verifyNoMoreInteractions(parameterRepository);
    }

    @Test
    void aPresentParameterIsStillReadOnlyOnce() {
        when(parameterRepository.getParameterValue("zap_mouse", "mass")).thenReturn(Optional.of(3.0));

        assertThat(parameterService.getValue("zap_mouse", "mass")).isEqualTo(3.0);
        assertThat(parameterService.getValue("zap_mouse", "mass")).isEqualTo(3.0);

        verify(parameterRepository).getParameterValue("zap_mouse", "mass");
        verifyNoMoreInteractions(parameterRepository);
    }

    @Test
    void invalidateCacheClearsNegativeEntriesToo() {
        when(parameterRepository.getParameterValue("zap_mouse", "radius"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(1.75));

        assertThat(parameterService.getValueOrDefault("zap_mouse", "radius", 0.0)).isZero();

        parameterService.invalidateCache();

        assertThat(parameterService.getValueOrDefault("zap_mouse", "radius", 0.0)).isEqualTo(1.75);
        verify(parameterRepository, times(2)).getParameterValue("zap_mouse", "radius");
    }

    @Test
    void callersThatDisagreeOnCaseShareOneCacheEntry() {
        when(parameterRepository.getParameterValue("zap_mouse", "mana_cost")).thenReturn(Optional.of(4.0));

        assertThat(parameterService.getValue("ZAP_MOUSE", "mana_cost")).isEqualTo(4.0);
        assertThat(parameterService.getValue("zap_mouse", "mana_cost")).isEqualTo(4.0);

        verify(parameterRepository).getParameterValue("zap_mouse", "mana_cost");
        verifyNoMoreInteractions(parameterRepository);
    }

    @Test
    void aMissStillThrowsFromGetValue() {
        when(parameterRepository.getParameterValue("zap_mouse", "panic_duration")).thenReturn(Optional.empty());

        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> parameterService.getValue("zap_mouse", "panic_duration"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parameter not found: zap_mouse, panic_duration");

        parameterService.getValueOrDefault("zap_mouse", "panic_duration", 1.0);
        verify(parameterRepository).getParameterValue("zap_mouse", "panic_duration");
        verifyNoMoreInteractions(parameterRepository);
    }
}
