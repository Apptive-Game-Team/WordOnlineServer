package com.wordonline.server.game.domain.object.prefab.implement.misc.third;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
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
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("tree_golem", "mass")));
        gameObject.addComponent(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("tree_golem", "radius"), false));
        gameObject.addComponent(new MeleeAttackMob(gameObject,
                (int) parameters.getValue("tree_golem", "hp"),
                (float) parameters.getValue("tree_golem", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("tree_golem", "damage"),
                (float) parameters.getValue("tree_golem", "attack_interval")
        ));

        gameObject.addComponent(new SelfHealer(
                gameObject,
                (int)  parameters.getValue("tree_golem", "heal_amount"),
                (float) parameters.getValue("tree_golem", "heal_interval"),
                ElementType.NATURE)
        );

        gameObject.addComponent(new PathSpawner(gameObject, PrefabType.LeafField, 1f));
        gameObject.setElement(ElementType.NATURE);
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
