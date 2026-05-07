package com.wordonline.server.game.domain.object.prefab.implement.drop;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Leafair;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("leafair_prefab")
public class LeafairPrefabInitializer extends PrefabInitializer {

    private static final float DEFAULT_TTL_RECOVER_AMOUNT = 3f;

    private final Parameters parameters;

    public LeafairPrefabInitializer(Parameters parameters) {
        super(PrefabType.Leafair);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        float radius = (float) parameters.getValue("drop", "radius");
        gameObject.addCollider(new CircleCollider(gameObject, radius, true));
        gameObject.setElement(EnumSet.of(ElementType.NATURE));
        int amount = (int) parameters.getValue("drop", "damage");
        gameObject.addComponent(new Leafair(gameObject, amount, amount, DEFAULT_TTL_RECOVER_AMOUNT, radius));
    }
}
