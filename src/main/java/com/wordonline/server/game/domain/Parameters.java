package com.wordonline.server.game.domain;

import com.wordonline.server.game.service.ParameterService;
import com.wordonline.server.game.service.ParameterProfileContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class Parameters {
    private final ParameterService parameterService;
    private final ParameterProfileContext parameterProfileContext;

    public double getValue(String gameObject, String parameter) {
        return parameterService.getValue(gameObject, parameter);
    }

    public void runWithProfile(Long parameterProfileId, Runnable action) {
        parameterProfileContext.runWithProfile(parameterProfileId, action);
    }

    public <T> T callWithProfile(Long parameterProfileId, Supplier<T> action) {
        return parameterProfileContext.callWithProfile(parameterProfileId, action);
    }
}
