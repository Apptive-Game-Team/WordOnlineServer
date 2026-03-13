package com.wordonline.server.game.service.pve;

import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.PVEBossMob;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.pve.PveInstallObject;
import com.wordonline.server.game.service.GameContext;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Scope("prototype")
public class PveScenarioInstaller {

    @Getter
    public static class RuntimeState {
        private final String stageId;
        private final Map<String, Integer> installedObjectIds = new HashMap<>();

        private RuntimeState(String stageId) {
            this.stageId = stageId;
        }

        public int getInstalledObjectId(String installerId) {
            return installedObjectIds.getOrDefault(installerId, -1);
        }
    }

    private static final float DEFAULT_BOSS_SPEED = 1.5f;
    private static final float DEFAULT_BOSS_ATTACK_RANGE = 7.0f;

    @Getter
    private RuntimeState runtime;

    public void install(String stageId, List<PveInstallObject> installers, GameContext gameContext) {
        this.runtime = new RuntimeState(stageId);

        for (PveInstallObject installObject : installers) {
            GameObject gameObject = new GameObject(
                    installObject.master(),
                    installObject.prefabType(),
                    installObject.position(),
                    gameContext
            );

            runtime.installedObjectIds.put(installObject.installerId(), gameObject.getId());

            List<Magic> usableMagics = installObject.magics() == null
                    ? List.of()
                    : installObject.magics().stream()
                    .filter(Objects::nonNull)
                    .toList();

            if (!usableMagics.isEmpty()) {
                // GameObject prefab is already initialized at this point.
                Mob existingMob = gameObject.getComponent(Mob.class);
                int maxHp = existingMob != null ? existingMob.getMaxHp() : 1;
                float speed = existingMob != null ? existingMob.getSpeed().total() : DEFAULT_BOSS_SPEED;

                if (existingMob != null) {
                    gameObject.removeComponent(existingMob);
                }

                // Ensure mobility components exist for BehaviorMob-based bosses.
                if (gameObject.getComponent(RigidBody.class) == null) {
                    gameObject.addComponent(new RigidBody(gameObject, 1));
                }
                if (gameObject.getComponent(ZPhysics.class) == null) {
                    gameObject.addComponent(new ZPhysics(gameObject));
                }

                float attackInterval = Math.max(0.6f, installObject.castIntervalSec());
                gameObject.addComponent(new PVEBossMob(
                        gameObject,
                        maxHp,
                        speed,
                        TargetMask.GROUND.bit,
                        attackInterval,
                        DEFAULT_BOSS_ATTACK_RANGE,
                        usableMagics
                ));
            }
        }
    }

    public GameObject getInstalledObject(GameContext gameContext, String installerId) {
        if (runtime == null || installerId == null || installerId.isBlank()) {
            return null;
        }

        int objectId = runtime.getInstalledObjectId(installerId);
        if (objectId < 0) {
            return null;
        }

        for (GameObject gameObject : gameContext.getGameObjects()) {
            if (gameObject.getId() == objectId) {
                return gameObject;
            }
        }
        return null;
    }
}


