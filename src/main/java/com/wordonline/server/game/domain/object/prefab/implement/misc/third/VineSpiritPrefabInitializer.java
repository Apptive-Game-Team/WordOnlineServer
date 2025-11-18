package com.wordonline.server.game.domain.object.prefab.implement.misc.third;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.EffectProvideProjectileRangeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

@Component("vine_spirit_prefab")
public class VineSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public VineSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.VineSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("vine_spirit", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("vine_spirit", "radius"), false));
        gameObject.getComponents().add(new EffectProvideProjectileRangeAttackMob(gameObject,
                (int) parameters.getValue("vine_spirit", "hp"),
                (float) parameters.getValue("vine_spirit", "speed"),
                TargetMask.ANY.bit,
                (int) parameters.getValue("vine_spirit", "damage"),
                (float) parameters.getValue("vine_spirit", "attack_interval"),
                (float) parameters.getValue("vine_spirit", "attack_range"),
                Effect.Snared,
                "NatureShot",
                0.5f
        ));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}
