package com.wordonline.server.game.domain.object.prefab.implement.build;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.component.SelfAttacker;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.simple.Turret;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("rock_turret_prefab")
public class RockTurretPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public RockTurretPrefabInitializer(Parameters parameters) {
        super(PrefabType.RockTurret);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("rock_turret", "mass")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("rock_turret", "radius"), false));
        gameObject.getComponents().add(new Turret(gameObject,
                (int) parameters.getValue("rock_turret", "hp"),
                (int) parameters.getValue("rock_turret", "damage"),
                TargetMask.GROUND.bit,
                0.2f,
                (float) parameters.getValue("rock_turret", "attack_interval"),
                (float) parameters.getValue("rock_turret", "attack_range")
        ));
        gameObject.addComponent(new SelfAttacker(gameObject, new AttackInfo(1, ElementType.ROCK), 1));
        gameObject.setElement(ElementType.ROCK);
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}
