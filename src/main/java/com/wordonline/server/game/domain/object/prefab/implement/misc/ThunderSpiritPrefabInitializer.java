package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.RangeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("thunder_spirit_prefab")
public class ThunderSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ThunderSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.ThunderSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("thunder_spirit", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("thunder_spirit", "radius"), false));
        gameObject.getComponents().add(new RangeAttackMob(gameObject,
                (int) parameters.getValue("thunder_spirit", "hp"),
                (float) parameters.getValue("thunder_spirit", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("thunder_spirit", "damage"),
                (float) parameters.getValue("thunder_spirit", "attack_interval"),
                (float) parameters.getValue("thunder_spirit", "attack_range")
        ));
        gameObject.setElement(EnumSet.of(ElementType.LIGHTNING,ElementType.WIND));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}