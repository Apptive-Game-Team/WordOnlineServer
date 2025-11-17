package com.wordonline.server.game.domain.object.component.mob.component;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;

public class SelfHealer extends SelfAttacker {

    public SelfHealer(GameObject gameObject, int healAmount, float healInterval,
            ElementType elementType) {
        super(gameObject, new AttackInfo(-healAmount, elementType), healInterval);
    }
}
