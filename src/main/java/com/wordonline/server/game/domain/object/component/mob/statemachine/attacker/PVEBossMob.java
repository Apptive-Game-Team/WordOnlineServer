package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.service.GameLoop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PVEBossMob extends BehaviorMob {

    private final List<Magic> magics;
    private final Map<Long, Integer> cooldownUntilFrame = new HashMap<>();

    public PVEBossMob(GameObject gameObject,
                      int maxHp,
                      float speed,
                      int targetMask,
                      float attackInterval,
                      float attackRange,
                      List<Magic> magics) {
        super(gameObject, maxHp, speed, targetMask, attackInterval, attackRange, null);
        this.magics = List.copyOf(magics);
        this.setBehavior(this::castMagic);
    }

    private boolean castMagic(GameObject target) {
        int currentFrame = getGameContext().getFrameNum();

        for (Magic magic : magics) {
            if (!isCooldownReady(magic, currentFrame)) {
                continue;
            }

            var result = getGameContext().getMagicInputHandler().handleBotMagicInput(
                    getGameContext(),
                    gameObject.getMaster(),
                    magic,
                    target.getPosition()
            );

            if (result.valid()) {
                setCooldown(magic, currentFrame);
                return true;
            }
        }

        return false;
    }

    private boolean isCooldownReady(Magic magic, int currentFrame) {
        return cooldownUntilFrame.getOrDefault(magic.id, 0) <= currentFrame;
    }

    private void setCooldown(Magic magic, int currentFrame) {
        int cooldownFrames = (int) Math.ceil(resolveCooldownSec(magic) * GameLoop.FPS);
        cooldownUntilFrame.put(magic.id, currentFrame + cooldownFrames);
    }

    private static double resolveCooldownSec(Magic magic) {
        return switch (magic.magicType) {
            case Shoot -> 1.8;
            case Explode -> 3.2;
            case Drop -> 4.0;
            case Spawn -> 4.8;
            case Build -> 6.0;
            default -> 3.0;
        };
    }
}
