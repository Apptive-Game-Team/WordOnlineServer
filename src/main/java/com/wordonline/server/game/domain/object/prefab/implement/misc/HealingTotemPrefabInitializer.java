package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.Cannon;
import com.wordonline.server.game.domain.object.component.mob.Totem;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.MeleeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("healing_totem_prefab")
public class HealingTotemPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public HealingTotemPrefabInitializer(Parameters parameters) {
        super(PrefabType.HealingTotem);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("healing_totem", "mass")));
//        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("healing_totem", "radius"), false));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("healing_totem", "radius"), true));
        gameObject.getComponents().add(new Totem(gameObject,
                (int) parameters.getValue("healing_totem", "hp"),
                (int) parameters.getValue("healing_totem", "damage"),
                (float)parameters.getValue("healing_totem", "attack_interval"),
                (float)parameters.getValue("healing_totem", "range"),
                TargetMask.GROUND.bit));
        gameObject.setElement(EnumSet.of(ElementType.NATURE,ElementType.WATER));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, (int) parameters.getValue("healing_totem", "duration")));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}