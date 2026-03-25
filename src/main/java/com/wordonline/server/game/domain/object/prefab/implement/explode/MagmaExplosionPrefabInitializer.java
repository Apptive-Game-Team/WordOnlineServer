package com.wordonline.server.game.domain.object.prefab.implement.explode;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

@Component("magma_explosion_prefab")
public class MagmaExplosionPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public MagmaExplosionPrefabInitializer(Parameters parameters) {
        super(PrefabType.MagmaExplosion);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(
                gameObject,
                (float) parameters.getValue("explode", "radius"),
                true
        ));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Explode(
                gameObject,
                (int) parameters.getValue("explode", "damage"),
                (float) parameters.getValue("explode", "radius")
        ));
    }
}
