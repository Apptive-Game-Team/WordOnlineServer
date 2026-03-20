package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;

import java.util.List;

public class VineWitchMob extends PVEBossMob {

    private static final int RAGE_CAST_COUNT = 20;

    private final Magic vineMagic;
    private boolean ragePatternActivated = false;
    private int remainingRageCastCount = 0;

    public VineWitchMob(GameObject gameObject,
                        int maxHp,
                        float speed,
                        int targetMask,
                        float attackInterval,
                        float attackRange,
                        List<Magic> magics,
                        Magic vineMagic) {
        super(gameObject, maxHp, speed, targetMask, attackInterval, attackRange, magics);
        this.vineMagic = vineMagic;
    }

    @Override
    public void onDamaged(AttackInfo attackInfo) {
        super.onDamaged(attackInfo);

        if (!ragePatternActivated && hp > 0 && hp <= maxHp / 2) {
            ragePatternActivated = true;
            remainingRageCastCount = RAGE_CAST_COUNT;
        }
    }

    @Override
    protected boolean castMagic(GameObject target) {
        if (remainingRageCastCount > 0) {
            if (vineMagic == null) {
                remainingRageCastCount = 0;
                return super.castMagic(target);
            }

            if (tryCastMagic(vineMagic, target)) {
                remainingRageCastCount--;
            }
            return true;
        }
        return super.castMagic(target);
    }
}
