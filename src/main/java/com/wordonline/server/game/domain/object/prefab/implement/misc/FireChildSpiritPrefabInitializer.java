package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.ProjectileRangeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("fire_child_spirit_prefab")
public class FireChildSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public FireChildSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.FireChildSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("fire_child_spirit", "mass")));
        gameObject.addComponent(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("fire_child_spirit", "radius"), false));
        gameObject.addComponent(new ProjectileRangeAttackMob(gameObject,
                (int) parameters.getValue("fire_child_spirit", "hp"),
                (float) parameters.getValue("fire_child_spirit", "speed"),
                TargetMask.ANY.bit,
                (int) parameters.getValue("fire_child_spirit", "damage"),
                (float) parameters.getValue("fire_child_spirit", "attack_interval"),
                (float) parameters.getValue("fire_child_spirit", "attack_range"),
                "FireShot",
                0.4f));
        gameObject.setElement(ElementType.FIRE);
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
