package com.wordonline.server.game.domain.object.prefab.implement.nature;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("leaf_explode_prefab")
public class LeafExplodePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public LeafExplodePrefabInitializer(Parameters parameters) {
        super(PrefabType.LeafExplode);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new Explode(gameObject, (int) parameters.getValue("explode", "damage")));
    }
}