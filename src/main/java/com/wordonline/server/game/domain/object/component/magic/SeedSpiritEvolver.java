package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

public final class SeedSpiritEvolver {

    private SeedSpiritEvolver() {
    }

    public static int evolveAlliedSeedSpirits(
            GameObject source,
            float radius,
            PrefabType evolvedPrefabType
    ) {
        Master owner = source.getMaster();
        int evolvedCount = 0;

        for (GameObject target : source.getGameContext().overlapSphereAll(source, radius)) {
            if (!canEvolve(source, target, owner)) {
                continue;
            }

            Vector3 targetPosition = new Vector3(target.getPosition());
            Master targetMaster = target.getMaster();
            target.destroy();
            new GameObject(
                    targetMaster,
                    evolvedPrefabType,
                    targetPosition,
                    source.getGameContext()
            );
            evolvedCount++;
        }

        return evolvedCount;
    }

    private static boolean canEvolve(GameObject source, GameObject target, Master owner) {
        if (target == source || target.isDestroyed()) {
            return false;
        }
        if (owner != Master.None && target.getMaster() != owner) {
            return false;
        }
        return target.getType() == PrefabType.SeedSpirit;
    }
}
