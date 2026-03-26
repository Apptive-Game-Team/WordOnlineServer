package com.wordonline.server.game.domain.object.prefab.implement.rock;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.RollingRockShot;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("rock_shot_prefab")
public class RockShotPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public RockShotPrefabInitializer(Parameters parameters) {
        super(PrefabType.RockShot);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        float radius = (float) parameters.getValue("shoot", "radius");
        gameObject.getColliders().add(new CircleCollider(gameObject, radius, true));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new RollingRockShot(gameObject,
                (int) parameters.getValue("shoot", "damage"),
                (float) parameters.getValue("shoot", "speed"),
                radius
        ));
    }
}
