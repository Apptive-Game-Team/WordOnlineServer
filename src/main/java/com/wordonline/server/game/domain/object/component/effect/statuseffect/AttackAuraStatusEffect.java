package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.dto.Effect;
import lombok.Getter;

public class AttackAuraStatusEffect extends BaseStatusEffect {
    @Getter
    private final ElementType element;

    public AttackAuraStatusEffect(GameObject owner, ElementType element, float duration) {
        super(owner, duration, StatusEffectKey.Default, toAttackAura(element));
        this.element = element;
    }

    @Override
    public void onAttacked(ElementType attackType) {
    }

    private static Effect toAttackAura(ElementType element) {
        return switch (element) {
            case FIRE -> Effect.FireAttackAura;
            case WATER -> Effect.WaterAttackAura;
            case NATURE -> Effect.NatureAttackAura;
            case LIGHTNING -> Effect.LightningAttackAura;
            case ROCK -> Effect.RockAttackAura;
            case WIND -> Effect.WindAttackAura;
            case NONE -> Effect.None;
        };
    }
}
