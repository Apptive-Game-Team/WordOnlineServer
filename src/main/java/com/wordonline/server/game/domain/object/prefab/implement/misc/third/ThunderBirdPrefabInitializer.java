package com.wordonline.server.game.domain.object.prefab.implement.misc.third;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.ThunderBirdMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("thunder_bird_prefab")
public class ThunderBirdPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ThunderBirdPrefabInitializer(Parameters parameters) {
        super(PrefabType.ThunderBird);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("thunder_bird", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("thunder_bird", "radius"), false));
        gameObject.getComponents().add(new ThunderBirdMob(gameObject,
                (int) parameters.getValue("thunder_bird", "hp"),
                (float) parameters.getValue("thunder_bird", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("thunder_bird", "damage"),
                (float) parameters.getValue("thunder_bird", "attack_interval"),
                (float) parameters.getValue("thunder_bird", "attack_range")
        ));
        gameObject.setElement(EnumSet.of(ElementType.LIGHTNING));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}
