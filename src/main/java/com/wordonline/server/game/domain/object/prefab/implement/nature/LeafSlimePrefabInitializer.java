package com.wordonline.server.game.domain.object.prefab.implement.nature;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.PathSpawner;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.Slime;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("leaf_slime_prefab")
public class LeafSlimePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    @Autowired
    public LeafSlimePrefabInitializer(Parameters parameters) {
        this(PrefabType.LeafSlime, parameters);
    }

    protected LeafSlimePrefabInitializer(PrefabType prefabType, Parameters parameters) {
        super(prefabType);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var slimeParameters = parameters.object(getGameObjectKey());
        gameObject.getComponents().add(new RigidBody(gameObject, slimeParameters.intValue(ParameterKey.MASS)));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, slimeParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.getComponents().add(new Slime(gameObject,
                slimeParameters.intValue(ParameterKey.HP),
                slimeParameters.floatValue(ParameterKey.SPEED),
                TargetMask.GROUND.bit,
                slimeParameters.intValue(ParameterKey.DAMAGE),
                slimeParameters.floatValue(ParameterKey.ATTACK_INTERVAL)));
        gameObject.getComponents().add(new PathSpawner(gameObject, PrefabType.LeafField, 1f));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }

    protected GameObjectKey getGameObjectKey() {
        return GameObjectKey.LEAF_SLIME;
    }
}
