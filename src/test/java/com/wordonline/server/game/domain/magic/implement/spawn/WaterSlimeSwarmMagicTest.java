package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.GameObjectParameters;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The swarm size lives in the {@code water_slime.quantity} database row, so the magic must not
 * carry a count of its own.
 */
class WaterSlimeSwarmMagicTest {

    @Test
    void summonsAsManyWaterSlimesAsTheQuantityParameter() {
        GameContext gameContext = mock(GameContext.class);
        List<GameObject> spawned = spawn(new WaterSlimeSwarmMagic(parameters(3)), gameContext, 3);

        assertThat(spawned).hasSize(3);
        assertThat(spawned).allMatch(slime -> slime.getType() == PrefabType.WaterSlime);
        assertThat(spawned).allMatch(slime -> slime.getMaster() == Master.LeftPlayer);
    }

    @Test
    void followsTheParameterWhenItChanges() {
        GameContext gameContext = mock(GameContext.class);
        List<GameObject> spawned = spawn(new WaterSlimeSwarmMagic(parameters(7)), gameContext, 7);

        assertThat(spawned).hasSize(7);
    }

    private List<GameObject> spawn(WaterSlimeSwarmMagic magic, GameContext gameContext, int expectedCount) {
        magic.run(gameContext, Master.LeftPlayer, new Vector3(4f, 0f, 6f));

        ArgumentCaptor<GameObject> created = ArgumentCaptor.forClass(GameObject.class);
        verify(gameContext, times(expectedCount)).createGameObject(created.capture());
        return created.getAllValues();
    }

    private Parameters parameters(int quantity) {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters objectParameters = mock(GameObjectParameters.class);
        when(parameters.object(GameObjectKey.WATER_SLIME)).thenReturn(objectParameters);
        when(objectParameters.intValue(ParameterKey.QUANTITY)).thenReturn(quantity);
        return parameters;
    }
}
