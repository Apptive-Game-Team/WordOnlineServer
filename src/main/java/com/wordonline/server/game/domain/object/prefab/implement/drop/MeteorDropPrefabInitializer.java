package com.wordonline.server.game.domain.object.prefab.implement.drop;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Drop;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("meteor_drop_prefab")
public class MeteorDropPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public MeteorDropPrefabInitializer(Parameters parameters) {
        super(PrefabType.MeteorDrop);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("meteor_drop", "radius"), true));
        gameObject.setElement(EnumSet.of(ElementType.FIRE, ElementType.ROCK));
        gameObject.getComponents().add(new Drop(gameObject, (int) parameters.getValue("meteor_drop", "damage")));
    }
}

