package com.wordonline.server.game.domain;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.dto.Effect;
import lombok.Data;

@Data
public class AttackInfo {
    private int damage;
    private ElementType element;
    private Effect effect;
    public AttackInfo(int damage) {
        this.damage = damage;
    }
    public AttackInfo(int damage, ElementType element) {
        this.damage = damage;
        this.element = element;
    }
}
