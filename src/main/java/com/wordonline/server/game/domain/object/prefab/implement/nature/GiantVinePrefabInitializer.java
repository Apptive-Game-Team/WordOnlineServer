package com.wordonline.server.game.domain.object.prefab.implement.nature;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.OnStartAttacker;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.magic.OnStartSeedSpiritEvolver;
import com.wordonline.server.game.domain.object.component.magic.VineSpawnContext;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("giant_vine_prefab")
public class GiantVinePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public GiantVinePrefabInitializer(Parameters parameters) {
        super(PrefabType.GiantVine);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var giantVineParameters = parameters.object(GameObjectKey.GIANT_VINE);
        float radius = giantVineParameters.floatValue(ParameterKey.RADIUS);

        gameObject.addCollider(new CircleCollider(gameObject, radius, true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.addComponent(new EffectProvider(gameObject, Effect.Snared));
        gameObject.addComponent(new TimedSelfDestroyer(
                gameObject,
                giantVineParameters.floatValue(ParameterKey.DURATION)
        ));
        gameObject.addComponent(new OnStartSeedSpiritEvolver(
                gameObject,
                radius,
                PrefabType.VineSpirit
        ));
        gameObject.addComponent(new OnStartAttacker(
                gameObject,
                radius,
                giantVineParameters.intValue(ParameterKey.DAMAGE),
                VineSpawnContext.currentTracker()
        ));
    }
}
