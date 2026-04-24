package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.simple.Cannon;
import com.wordonline.server.game.domain.object.component.mob.simple.ManaWellMob;
import com.wordonline.server.game.domain.object.component.mob.simple.PlayerHealthComponent;
import com.wordonline.server.game.domain.object.component.mob.simple.Totem;
import com.wordonline.server.game.domain.object.component.mob.simple.Turret;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;

public class FrenzyStatusEffect extends BaseStatusEffect {
    private Master originalMaster;

    public FrenzyStatusEffect(GameObject owner, float duration, StatusEffectKey key) {
        super(owner, duration, key);
    }

    @Override
    public void start() {
        if (originalMaster == null) {
            if (!isControllableSummon()) {
                gameObject.removeComponent(this);
                return;
            }
            originalMaster = gameObject.getMaster();
            gameObject.setMaster(Master.None);
        }

        gameObject.setEffect(Effect.Frenzy);
    }

    private boolean isControllableSummon() {
        if (gameObject.getMaster() == Master.None) return false;
        if (!gameObject.hasComponent(Mob.class)) return false;

        return !gameObject.hasComponent(PlayerHealthComponent.class)
                && !gameObject.hasComponent(Cannon.class)
                && !gameObject.hasComponent(ManaWellMob.class)
                && !gameObject.hasComponent(Totem.class)
                && !gameObject.hasComponent(Turret.class);
    }

    @Override
    public void onAttacked(ElementType attackType) {
    }

    @Override
    protected void expire() {
        if (originalMaster != null && gameObject.getMaster() == Master.None) {
            gameObject.setMaster(originalMaster);
        }
        super.expire();
    }
}
