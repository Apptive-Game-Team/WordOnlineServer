package com.wordonline.server.game.domain.parameter;

import com.wordonline.server.game.domain.Parameters;

public class GameObjectParameters {

    private final String gameObjectName;
    private final Parameters parameters;

    public GameObjectParameters(GameObjectKey gameObjectKey, Parameters parameters) {
        this(gameObjectKey.dbName(), parameters);
    }

    /**
     * 데이터에서 읽은 game object 이름으로 만든다. {@link GameObjectKey} 에 없는 이름도 있기
     * 때문에 이 경로가 필요하다 — 마법이 가리키는 game object 는 행이 늘어나기만 한다.
     */
    public GameObjectParameters(String gameObjectName, Parameters parameters) {
        this.gameObjectName = gameObjectName;
        this.parameters = parameters;
    }

    public double doubleValue(ParameterKey parameterKey) {
        return parameters.getValue(gameObjectName, parameterKey.dbName());
    }

    public int intValue(ParameterKey parameterKey) {
        return (int) doubleValue(parameterKey);
    }

    public int intValueOrDefault(ParameterKey parameterKey, int defaultValue) {
        return (int) parameters.getValueOrDefault(gameObjectName, parameterKey.dbName(), defaultValue);
    }

    public long longValue(ParameterKey parameterKey) {
        return (long) doubleValue(parameterKey);
    }

    public float floatValue(ParameterKey parameterKey) {
        return (float) doubleValue(parameterKey);
    }
}
