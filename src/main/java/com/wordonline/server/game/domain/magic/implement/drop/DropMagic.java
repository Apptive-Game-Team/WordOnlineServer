package com.wordonline.server.game.domain.magic.implement.drop;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

/**
 * {@code cast_kind = 'Drop'} 인 마법. 조준점 위 spawn height 에서 떨어뜨린다.
 *
 * <p>bean 이 아니라 {@code magics} 행에서 만들어진다.
 */
public final class DropMagic extends AbstractDropMagic {

    public DropMagic(PrefabType prefabType, float initialHeight) {
        super(prefabType, initialHeight);
    }
}
