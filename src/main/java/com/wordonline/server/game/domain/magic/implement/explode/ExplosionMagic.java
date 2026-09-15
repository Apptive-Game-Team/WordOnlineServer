package com.wordonline.server.game.domain.magic.implement.explode;

import com.wordonline.server.game.domain.object.prefab.PrefabType;

/**
 * {@code cast_kind = 'Explosion'} 인 마법. 조준점에 폭발 오브젝트 하나를 만든다.
 *
 * <p>bean 이 아니라 {@code magics} 행에서 만들어진다.
 */
public final class ExplosionMagic extends AbstractExplosionMagic {

    public ExplosionMagic(PrefabType prefabType) {
        super(prefabType);
    }
}
