package com.wordonline.server.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.GameObjectParameters;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.repository.ParameterRepository;

class GameObjectParametersTest {

    private final ParameterRepository parameterRepository = mock(ParameterRepository.class);
    private final Parameters parameters = new Parameters(new ParameterService(parameterRepository));

    @Test
    void returnsSameValueAsRawLookupThroughSharedCache() {
        when(parameterRepository.getParameterValue("zap_mouse", "radius"))
                .thenReturn(Optional.of(1.75));

        double rawValue = parameters.getValue("zap_mouse", "radius");
        double typedValue = parameters.object(GameObjectKey.ZAP_MOUSE).doubleValue(ParameterKey.RADIUS);

        assertThat(typedValue).isEqualTo(rawValue);
        verify(parameterRepository).getParameterValue("zap_mouse", "radius");
        verifyNoMoreInteractions(parameterRepository);
    }

    @Test
    void numericConversionsMatchJavaCastSemantics() {
        when(parameterRepository.getParameterValue("zap_mouse", "radius"))
                .thenReturn(Optional.of(1.75));

        GameObjectParameters zapMouseParameters = parameters.object(GameObjectKey.ZAP_MOUSE);

        assertThat(zapMouseParameters.doubleValue(ParameterKey.RADIUS)).isEqualTo(1.75);
        assertThat(zapMouseParameters.intValue(ParameterKey.RADIUS)).isEqualTo(1);
        assertThat(zapMouseParameters.longValue(ParameterKey.RADIUS)).isEqualTo(1L);
        assertThat(zapMouseParameters.floatValue(ParameterKey.RADIUS)).isEqualTo(1.75f);
    }

    @Test
    void missingParametersKeepExistingExceptionBehavior() {
        when(parameterRepository.getParameterValue("zap_mouse", "panic_duration"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> parameters.object(GameObjectKey.ZAP_MOUSE).floatValue(ParameterKey.PANIC_DURATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parameter not found: zap_mouse, panic_duration");
    }
}
