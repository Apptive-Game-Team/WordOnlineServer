package com.wordonline.server.game.domain.magic.implement.spawn;

import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectParameters;

/**
 * {@code cast_kind = 'Spawn'} 인 마법. 조준점에 유닛을 quantity 마리 만든다.
 *
 * <p>bean 이 아니라 {@code magics} 행에서 만들어진다. quantity 는 마법이 가리키는 game object 의
 * parameter 에서 읽는다.
 */
public final class SpawnMagic extends AbstractSpawnMagic {

    public SpawnMagic(PrefabType prefabType, GameObjectParameters parameters, float spawnHeight) {
        super(prefabType, parameters, spawnHeight);
    }
}
