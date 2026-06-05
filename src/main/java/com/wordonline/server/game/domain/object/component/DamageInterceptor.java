package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.AttackInfo;

public interface DamageInterceptor {
    boolean beforeDamage(AttackInfo attackInfo);
}
