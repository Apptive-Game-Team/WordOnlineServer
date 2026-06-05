package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

public class VineToss extends Shot {

    public VineToss(GameObject gameObject) {
        super(gameObject, 0, 0f);
    }

    @Override
    public void setTarget(Vector3 targetPosition) {
        var vineTossParameters = getGameContext().getParameters().object(GameObjectKey.VINE_TOSS);
        int vineCount = vineTossParameters.intValue(ParameterKey.VINE_COUNT);
        float vineSpacing = vineTossParameters.floatValue(ParameterKey.VINE_SPACING);
        float vineSpawnInterval = vineTossParameters.floatValue(ParameterKey.VINE_SPAWN_INTERVAL);

        Vector3 direction = targetPosition.subtract(gameObject.getPosition()).normalize();
        if (direction.equals(Vector3.ZERO)) {
            gameObject.destroy();
            return;
        }

        VineHitTracker hitTracker = new VineHitTracker();
        Vector3 firstPosition = gameObject.getPosition().plus(direction.multiply(vineSpacing));
        final GameObject[] firstVineRef = new GameObject[1];
        VineSpawnContext.runWithTracker(hitTracker, () ->
                firstVineRef[0] = new GameObject(gameObject.getMaster(), PrefabType.Vine, firstPosition, getGameContext())
        );
        GameObject firstVine = firstVineRef[0];
        firstVine.addComponent(new SequentialLineSpawner(
                firstVine,
                gameObject.getMaster(),
                PrefabType.Vine,
                firstPosition,
                direction,
                vineSpacing,
                vineSpawnInterval,
                vineCount,
                hitTracker
        ));

        gameObject.destroy();
    }
}
