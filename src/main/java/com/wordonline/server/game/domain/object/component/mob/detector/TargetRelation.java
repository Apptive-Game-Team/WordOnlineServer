package com.wordonline.server.game.domain.object.component.mob.detector;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.FrenzyStatusEffect;

public final class TargetRelation {

    private TargetRelation() {
    }

    public static boolean canAttack(GameObject self, GameObject target) {
        if (target == self) {
            return false;
        }

        // a dying object is already lost, so nobody wastes attacks on it
        if (target.isDying()) {
            return false;
        }

        return FrenzyStatusEffect.isActiveOn(self)
                || target.getMaster() != self.getMaster();
    }
}
