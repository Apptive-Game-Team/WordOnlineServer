package com.wordonline.server.game.domain.object.prefab.implement.drop;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.magic.LightningStrike;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("lightning_drop_prefab")
public class LightningDropPrefabInitializer extends PrefabInitializer {

    private static final float STRIKE_VISUAL_DURATION = 0.1f;

    private final Parameters parameters;

    public LightningDropPrefabInitializer(Parameters parameters) {
        super(PrefabType.LightningDrop);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var dropParameters = parameters.object(GameObjectKey.DROP);
        float radius = dropParameters.floatValue(ParameterKey.RADIUS);

        gameObject.addCollider(new CircleCollider(gameObject, radius, true));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.getComponents().add(new LightningStrike(
                gameObject,
                dropParameters.intValue(ParameterKey.DAMAGE),
                radius
        ));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, STRIKE_VISUAL_DURATION));
    }
}
