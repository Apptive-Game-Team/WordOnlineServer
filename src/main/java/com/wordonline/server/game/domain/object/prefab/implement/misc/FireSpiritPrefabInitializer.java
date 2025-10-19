package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.SummonerMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("fire_spirit_prefab")
public class FireSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public FireSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.FireSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getComponents().add(new RigidBody(gameObject, (int) parameters.getValue("fire_spirit", "mass")));
        gameObject.getComponents().add(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("fire_spirit", "radius"), false));
        gameObject.getComponents().add(new SummonerMob(gameObject,
                (int) parameters.getValue("fire_spirit", "hp"),
                (float) parameters.getValue("fire_spirit", "speed"),
                TargetMask.GROUND.bit,
                (float) parameters.getValue("fire_spirit", "attack_interval"),
                (float) parameters.getValue("fire_spirit", "attack_range"),
                PrefabType.FireField
        ));
        gameObject.setElement(EnumSet.of(ElementType.FIRE,ElementType.WIND));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}