package com.wordonline.server.game.domain.parameter;

import com.wordonline.server.game.domain.Parameters;

public class GameObjectParameters {

    private final GameObjectKey gameObjectKey;
    private final Parameters parameters;

    public GameObjectParameters(GameObjectKey gameObjectKey, Parameters parameters) {
        this.gameObjectKey = gameObjectKey;
        this.parameters = parameters;
    }

    public double doubleValue(ParameterKey parameterKey) {
        return parameters.getValue(gameObjectKey, parameterKey);
    }

    public int intValue(ParameterKey parameterKey) {
        return (int) doubleValue(parameterKey);
    }

    public int intValueOrDefault(ParameterKey parameterKey, int defaultValue) {
        return (int) parameters.getValueOrDefault(gameObjectKey, parameterKey, defaultValue);
    }

    public long longValue(ParameterKey parameterKey) {
        return (long) doubleValue(parameterKey);
    }

    public float floatValue(ParameterKey parameterKey) {
        return (float) doubleValue(parameterKey);
    }
}
