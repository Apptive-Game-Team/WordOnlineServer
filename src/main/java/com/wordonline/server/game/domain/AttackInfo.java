package com.wordonline.server.game.domain;

import com.wordonline.server.game.dto.Effect;
import lombok.Data;

@Data
public class AttackInfo {
    private int damage;
    private Effect effect;
    public AttackInfo(int damage) {
        this.damage = damage;
    }
}
