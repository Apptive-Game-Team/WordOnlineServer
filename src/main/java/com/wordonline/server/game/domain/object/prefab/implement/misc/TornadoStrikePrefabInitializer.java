package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
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
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("tornado_strike", "radius"), true));
        gameObject.setElement(EnumSet.of(ElementType.NATURE,ElementType.WIND));
        gameObject.getComponents().add(new Tornado(gameObject,
                (float) parameters.getValue("tornado_strike", "speed"),
                (int) parameters.getValue("tornado_strike", "damage"),
                (float) parameters.getValue("tornado_strike", "radius"),
                (float) parameters.getValue("tornado_strike", "duration"),
                (float) parameters.getValue("tornado_strike", "attack_interval")
                ));
//        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Shock));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("tornado_strike", "duration")));
    }
}