package com.wordonline.server.game.domain.magic.implement.shoot;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

/**
 * {@code cast_kind = 'Shot'} 인 마법. 시전자 위치에서 조준 방향으로 투사체 하나를 만든다.
 *
 * <p>bean 이 아니라 {@code magics} 행에서 만들어진다.
 */
public final class ShotMagic extends AbstractShotMagic {

    public ShotMagic(PrefabType prefabType) {
        super(prefabType);
    }
}
