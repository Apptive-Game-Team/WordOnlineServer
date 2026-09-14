package com.wordonline.server.game.domain.magic.implement.build;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

/**
 * {@code cast_kind = 'Summon'} 인 마법. 조준점에 건물 하나를 놓는다.
 *
 * <p>bean 이 아니라 {@code magics} 행에서 만들어진다 — 클래스가 담던 것이 prefab 하나와
 * spawn height 하나뿐이라 데이터로 옮겼다.
 */
public final class SummonMagic extends AbstractSummonMagic {

    public SummonMagic(PrefabType prefabType, Float spawnHeight) {
        super(prefabType, spawnHeight);
    }
}
