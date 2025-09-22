package com.wordonline.server.game.domain.object.component.mob.detector;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.object.GameObject;

public enum TargetMask {
    GROUND(1), AIR(2), ANY(GROUND.bit | AIR.bit);
    public final int bit;
    TargetMask(int bit) { this.bit = bit; }

    public static int of(GameObject target) {
        return target.getPosition().getZ() >= GameConfig.AERIAL_STANDARD_HEIGHT ? AIR.bit : GROUND.bit;
    }
}
