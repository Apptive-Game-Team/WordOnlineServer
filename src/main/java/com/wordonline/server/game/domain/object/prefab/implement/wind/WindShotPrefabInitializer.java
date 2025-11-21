package com.wordonline.server.game.domain.object.prefab.implement.wind;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.KnockbackEffectProvider;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("wind_shot_prefab")
public class WindShotPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public WindShotPrefabInitializer(Parameters parameters) {
        super(PrefabType.WindShot);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("shoot", "radius"), true));
        gameObject.setElement(ElementType.WIND);
        gameObject.getComponents().add(new KnockbackEffectProvider(gameObject, Effect.Knockback));
        gameObject.getComponents().add(new Shot(gameObject,
                (int) parameters.getValue("shoot", "damage"),
                (float) parameters.getValue("shoot", "speed")
        ));
    }
}