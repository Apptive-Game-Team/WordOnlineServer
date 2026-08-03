package com.wordonline.server.game.domain.object.prefab.implement.nature;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.Spawner;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import org.springframework.stereotype.Component;

@Component("seed_nest_prefab")
public class SeedNestPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public SeedNestPrefabInitializer(Parameters parameters) {
        super(PrefabType.SeedNest);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var seedNestParameters = parameters.object(GameObjectKey.SEED_NEST);

        gameObject.addCollider(new CircleCollider(
                gameObject,
                seedNestParameters.floatValue(ParameterKey.RADIUS),
                false
        ));
        gameObject.setElement(ElementType.NATURE);
        gameObject.addComponent(new Spawner(
                gameObject,
                seedNestParameters.intValue(ParameterKey.HP),
                PrefabType.SeedSpirit,
                seedNestParameters.floatValue(ParameterKey.ATTACK_INTERVAL),
                true,
                1
        ));
        gameObject.addComponent(new TimedSelfDestroyer(
                gameObject,
                seedNestParameters.floatValue(ParameterKey.DURATION)
        ));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
