package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.magic.Tornado;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("tornado_strike_prefab")
public class TornadoStrikePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public TornadoStrikePrefabInitializer(Parameters parameters) {
        super(PrefabType.TornadoStrike);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var tornadoStrikeParameters = parameters.object(GameObjectKey.TORNADO_STRIKE);
        gameObject.addCollider(new CircleCollider(gameObject, tornadoStrikeParameters.floatValue(ParameterKey.RADIUS), true));
        gameObject.setElement(EnumSet.of(ElementType.NATURE,ElementType.WIND));
        gameObject.getComponents().add(new Tornado(gameObject,
                tornadoStrikeParameters.floatValue(ParameterKey.SPEED),
                tornadoStrikeParameters.intValue(ParameterKey.DAMAGE),
                tornadoStrikeParameters.floatValue(ParameterKey.RADIUS),
                tornadoStrikeParameters.floatValue(ParameterKey.DURATION),
                tornadoStrikeParameters.floatValue(ParameterKey.ATTACK_INTERVAL)
                ));
//        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, tornadoStrikeParameters.floatValue(ParameterKey.DURATION)));
    }
}