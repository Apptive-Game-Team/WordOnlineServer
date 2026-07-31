package com.wordonline.server.game.domain.object.prefab.implement.rock;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.StaticObstacle;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;

/**
 * Rubble left behind by a rock creature or rock building that died in combat.
 * It only blocks ground movement: no movement, no attack, no hp.
 */
@Component("rock_remnant_prefab")
public class RockRemnantPrefabInitializer extends PrefabInitializer {

    private static final float TIME_TO_LIVE_SECONDS = 20f;

    private final Parameters parameters;

    public RockRemnantPrefabInitializer(Parameters parameters) {
        super(PrefabType.RockRemnant);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        // Shares the MiniRock footprint; a dedicated parameter row would need a data migration.
        var miniRockParameters = parameters.object(GameObjectKey.MINI_ROCK);
        gameObject.addCollider(new CircleCollider(gameObject, miniRockParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.getComponents().add(new StaticObstacle(gameObject));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, TIME_TO_LIVE_SECONDS));
        gameObject.setElement(ElementType.ROCK);
    }
}
