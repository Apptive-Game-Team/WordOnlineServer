package com.wordonline.server.game.domain;

import com.wordonline.server.game.service.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Parameters {
    private final ParameterService parameterService;

    public double getValue(String gameObject, String parameter) {
        return parameterService.getValue(gameObject, parameter);
    }
}
