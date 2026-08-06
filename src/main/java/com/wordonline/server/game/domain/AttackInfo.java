package com.wordonline.server.game.domain;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Effect;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class AttackInfo {
    private int damage;
    private Set<ElementType> element = new HashSet<>();
    private Effect effect;
    // id of the GameObject that dealt this hit; 0 when the source is not a single object
    private int attackerId;


    public AttackInfo(int damage, ElementType element) {
        this.damage = damage;
        this.element.add(element);
    }

    public AttackInfo(int damage, Set<ElementType> elementSet) {
        this.damage = damage;
        this.element = elementSet;
    }

    public AttackInfo withAttacker(GameObject attacker) {
        this.attackerId = attacker.getId();
        return this;
    }
}
