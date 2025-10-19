package com.wordonline.server.game.domain.object.prefab.implement.rock;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("rock_explode_prefab")
public class RockExplodePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public RockExplodePrefabInitializer(Parameters parameters) {
        super(PrefabType.RockExplode);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new Explode(gameObject, (int) parameters.getValue("explode", "damage")));
    }
}