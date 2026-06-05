package com.wordonline.server.game.domain.object.prefab.implement.explode;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.magic.RazorGale;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("razor_gale_prefab")
public class RazorGalePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public RazorGalePrefabInitializer(Parameters parameters) {
        super(PrefabType.RazorGale);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var razorGaleParameters = parameters.object(GameObjectKey.RAZOR_GALE);
        float radius = razorGaleParameters.floatValue(ParameterKey.RADIUS);
        gameObject.addCollider(new CircleCollider(gameObject, radius, true));
        gameObject.setElement(ElementType.WIND);
        gameObject.addComponent(new RazorGale(
                gameObject,
                razorGaleParameters.intValue(ParameterKey.DAMAGE),
                radius,
                razorGaleParameters.floatValue(ParameterKey.ATTACK_INTERVAL)));
        gameObject.addComponent(new TimedSelfDestroyer(gameObject, razorGaleParameters.floatValue(ParameterKey.DURATION)));
    }
}
