package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

public class VineShot extends Shot {

    private static final int VINE_COUNT = 6;
    private static final float VINE_SPACING = 1f;
    private static final float VINE_SPAWN_INTERVAL = 0.12f;

    public VineShot(GameObject gameObject) {
        super(gameObject, 0, 0f);
    }

    @Override
    public void setTarget(Vector3 targetPosition) {
        Vector3 direction = targetPosition.subtract(gameObject.getPosition()).normalize();
        if (direction.equals(Vector3.ZERO)) {
            gameObject.destroy();
            return;
        }

        Vector3 firstPosition = gameObject.getPosition().plus(direction.multiply(VINE_SPACING));
        GameObject firstVine = new GameObject(gameObject.getMaster(), PrefabType.Vine, firstPosition, getGameContext());
        firstVine.addComponent(new SequentialLineSpawner(
                firstVine,
                gameObject.getMaster(),
                PrefabType.Vine,
                firstPosition,
                direction,
                VINE_SPACING,
                VINE_SPAWN_INTERVAL,
                VINE_COUNT
        ));

        gameObject.destroy();
    }
}
