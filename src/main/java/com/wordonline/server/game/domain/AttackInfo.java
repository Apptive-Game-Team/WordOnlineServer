package com.wordonline.server.game.domain;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.dto.Effect;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class AttackInfo {
    private int damage;
    private Set<ElementType> element = new HashSet<>();
    private Effect effect;
    
    public AttackInfo(int damage, ElementType element) {
        this.damage = damage;
        this.element.add(element);
    }

    public AttackInfo(int damage, Set<ElementType> elementSet) {
        this.damage = damage;
        this.element = elementSet;
    }
}
