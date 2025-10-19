package com.wordonline.server.game.domain.object.prefab.implement.fire;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("fire_shot_prefab")
public class FireShotPrefabInitializer extends PrefabInitializer {
    private final Parameters parameters;

    public FireShotPrefabInitializer(Parameters parameters) {
        super(PrefabType.FireShot);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("shoot", "radius"), true));
        gameObject.setElement(ElementType.FIRE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Burn));
        gameObject.getComponents().add(new Shot(gameObject, (int) parameters.getValue("shoot", "damage")));
    }
}
