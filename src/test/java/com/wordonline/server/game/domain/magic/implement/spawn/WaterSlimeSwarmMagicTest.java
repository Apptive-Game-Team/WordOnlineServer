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

class WaterSlimeSwarmMagicTest {

    @Test
    void summonsExactlyThreeWaterSlimesRegardlessOfQuantityParameter() {
        GameContext gameContext = mock(GameContext.class);
        List<GameObject> spawned = spawn(new WaterSlimeSwarmMagic(parameters(GameObjectKey.WATER_SLIME, 8)), gameContext, 3);

        assertThat(spawned).hasSize(3);
        assertThat(spawned).allMatch(slime -> slime.getType() == PrefabType.WaterSlime);
        assertThat(spawned).allMatch(slime -> slime.getMaster() == Master.LeftPlayer);
    }

    @Test
    void keepsParameterDrivenCountForOtherSwarms() {
        GameContext gameContext = mock(GameContext.class);
        List<GameObject> spawned = spawn(new MiniRockSwarmMagic(parameters(GameObjectKey.MINI_ROCK, 5)), gameContext, 5);

        assertThat(spawned).hasSize(5);
        assertThat(spawned).allMatch(rock -> rock.getType() == PrefabType.MiniRock);
    }

    private List<GameObject> spawn(AbstractSwarmSpawnMagic magic, GameContext gameContext, int expectedCount) {
        magic.run(gameContext, Master.LeftPlayer, new Vector3(4f, 0f, 6f));

        ArgumentCaptor<GameObject> created = ArgumentCaptor.forClass(GameObject.class);
        verify(gameContext, times(expectedCount)).createGameObject(created.capture());
        return created.getAllValues();
    }

    private Parameters parameters(GameObjectKey key, int quantity) {
        Parameters parameters = mock(Parameters.class);
        GameObjectParameters objectParameters = mock(GameObjectParameters.class);
        when(parameters.object(key)).thenReturn(objectParameters);
        when(objectParameters.intValue(ParameterKey.QUANTITY)).thenReturn(quantity);
        return parameters;
    }
}
