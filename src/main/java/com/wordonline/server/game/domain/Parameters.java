package com.wordonline.server.game.domain;

import java.util.EnumMap;
import java.util.Map;

import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.GameObjectParameters;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.service.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Parameters {
    private final ParameterService parameterService;
    private final Map<GameObjectKey, GameObjectParameters> gameObjectParameters = new EnumMap<>(GameObjectKey.class);

    public GameObjectParameters object(GameObjectKey gameObject) {
        return gameObjectParameters.computeIfAbsent(gameObject, key -> new GameObjectParameters(key, this));
    }

    public double getValue(GameObjectKey gameObject, ParameterKey parameter) {
        return getValue(gameObject.dbName(), parameter.dbName());
    }

    public double getValue(String gameObject, String parameter) {
        return parameterService.getValue(gameObject, parameter);
    }

    public double getValueOrDefault(GameObjectKey gameObject, ParameterKey parameter, double defaultValue) {
        return parameterService.getValueOrDefault(gameObject.dbName(), parameter.dbName(), defaultValue);
    }
}
