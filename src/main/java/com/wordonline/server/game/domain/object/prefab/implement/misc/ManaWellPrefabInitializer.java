package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.simple.ManaWellMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("mana_well_prefab")
public class ManaWellPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ManaWellPrefabInitializer(Parameters parameters) {
        super(PrefabType.ManaWell);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var manaWellParameters = parameters.object(GameObjectKey.MANA_WELL);
        gameObject.addCollider(new CircleCollider(gameObject, manaWellParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.getComponents().add(new ManaWellMob(gameObject,
                manaWellParameters.intValue(ParameterKey.HP)
        ));
        gameObject.addComponent(new TimedSelfDestroyer(
                gameObject,
                (int)(manaWellParameters.doubleValue(ParameterKey.HP) * manaWellParameters.doubleValue(ParameterKey.ATTACK_INTERVAL) / manaWellParameters.doubleValue(ParameterKey.DAMAGE))
        ));
        gameObject.setElement(EnumSet.of(ElementType.LIGHTNING, ElementType.NATURE));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}