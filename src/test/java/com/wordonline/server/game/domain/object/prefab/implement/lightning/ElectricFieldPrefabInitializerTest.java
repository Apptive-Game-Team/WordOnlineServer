package com.wordonline.server.game.domain.object.prefab.implement.lightning;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.GameObjectParameters;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ElectricFieldPrefabInitializerTest {

    @Test
    void usesConfiguredElectricFieldRadiusAndDuration() {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters electricField = mock(GameObjectParameters.class);
        GameObject field = mock(GameObject.class);
        when(parameters.object(GameObjectKey.ELECTRIC_FIELD)).thenReturn(electricField);
        when(electricField.floatValue(ParameterKey.RADIUS)).thenReturn(4f);
        when(electricField.floatValue(ParameterKey.DURATION)).thenReturn(5f);
        when(field.getPosition()).thenReturn(new Vector3(1f, 2f, 3f));
        when(field.getComponents()).thenReturn(new ArrayList<Component>());

        new ElectricFieldPrefabInitializer(parameters).initialize(field);

        verify(parameters).object(GameObjectKey.ELECTRIC_FIELD);
        verify(electricField).floatValue(ParameterKey.RADIUS);
        verify(electricField).floatValue(ParameterKey.DURATION);
    }
}
