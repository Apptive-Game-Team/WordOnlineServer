package com.wordonline.server.game.domain.object.prefab.implement.explode;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.KnockbackEffectProvider;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("wind_explode_prefab")
public class WindExplodePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public WindExplodePrefabInitializer(Parameters parameters) {
        super(PrefabType.WindExplode);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("explode", "radius"), true));
        gameObject.setElement(ElementType.WIND);
        gameObject.getComponents().add(new KnockbackEffectProvider(gameObject, Effect.Knockback));
        gameObject.getComponents().add(new Explode(
                gameObject,
                (int) parameters.getValue("wind_shoot", "damage"),
                (float) parameters.getValue("explode", "radius")));
    }
}