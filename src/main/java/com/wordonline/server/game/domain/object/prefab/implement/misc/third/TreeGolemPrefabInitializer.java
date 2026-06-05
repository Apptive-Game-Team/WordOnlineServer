package com.wordonline.server.game.domain.object.prefab.implement.misc.third;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.PathSpawner;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.component.SelfHealer;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.MeleeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("tree_golem_prefab")
public class TreeGolemPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public TreeGolemPrefabInitializer(Parameters parameters) {
        super(PrefabType.TreeGolem);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var treeGolemParameters = parameters.object(GameObjectKey.TREE_GOLEM);
        gameObject.addComponent(new RigidBody(gameObject, treeGolemParameters.intValue(ParameterKey.MASS)));
        gameObject.addComponent(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, treeGolemParameters.floatValue(ParameterKey.RADIUS), false));
        gameObject.addComponent(new MeleeAttackMob(gameObject,
                treeGolemParameters.intValue(ParameterKey.HP),
                treeGolemParameters.floatValue(ParameterKey.SPEED),
                TargetMask.GROUND.bit,
                treeGolemParameters.intValue(ParameterKey.DAMAGE),
                treeGolemParameters.floatValue(ParameterKey.ATTACK_INTERVAL)
        ));

        gameObject.addComponent(new SelfHealer(
                gameObject,
                treeGolemParameters.intValue(ParameterKey.HEAL_AMOUNT),
                treeGolemParameters.floatValue(ParameterKey.HEAL_INTERVAL),
                ElementType.NATURE)
        );

        gameObject.addComponent(new PathSpawner(gameObject, PrefabType.LeafField, 1f));
        gameObject.setElement(ElementType.NATURE);
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
