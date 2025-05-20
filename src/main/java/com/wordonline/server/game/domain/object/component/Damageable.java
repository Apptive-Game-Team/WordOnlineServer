package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.AttackInfo;

public interface Damageable {
    void onDamaged(AttackInfo attackInfo);
}
