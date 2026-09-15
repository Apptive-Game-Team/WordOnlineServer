package com.wordonline.server.game.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, GameObjectParameters> namedGameObjectParameters = new ConcurrentHashMap<>();

    public GameObjectParameters object(GameObjectKey gameObject) {
        return gameObjectParameters.computeIfAbsent(gameObject, key -> new GameObjectParameters(key, this));
    }

    /**
     * 데이터에서 읽은 game object 이름으로 찾는다. {@link GameObjectKey} 에 없는 이름도 있기
     * 때문에 이 경로가 필요하다.
     */
    public GameObjectParameters objectByName(String gameObjectName) {
        return namedGameObjectParameters.computeIfAbsent(
                gameObjectName, name -> new GameObjectParameters(name, this));
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

    public double getValueOrDefault(String gameObject, String parameter, double defaultValue) {
        return parameterService.getValueOrDefault(gameObject, parameter, defaultValue);
    }
}
