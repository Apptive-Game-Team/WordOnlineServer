package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.LimitedSequenceSpawner;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.CowardMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("dimension_toad_prefab")
public class DimensionToadPrefabInitializer extends PrefabInitializer {

    private static final float TADPOLE_SPAWN_INTERVAL_SEC = 1f;
    private static final int MAX_TADPOLE_SPAWN_COUNT = 6;

    private final Parameters parameters;

    public DimensionToadPrefabInitializer(Parameters parameters) {
        super(PrefabType.DimensionToad);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("dimension_toad", "mass")));
        gameObject.addComponent(new ZPhysics(gameObject));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("dimension_toad", "radius"), false));
        gameObject.addComponent(new CowardMob(gameObject,
                (int) parameters.getValue("dimension_toad", "hp"),
                (float) parameters.getValue("dimension_toad", "speed"),
                TargetMask.GROUND.bit,
                (int) parameters.getValue("dimension_toad", "damage"),
                (float) parameters.getValue("dimension_toad", "attack_interval"),
                (float) parameters.getValue("dimension_toad", "detection_range"),
                (float) parameters.getValue("dimension_toad", "panic_duration")));
        gameObject.addComponent(new LimitedSequenceSpawner(
                gameObject,
                TADPOLE_SPAWN_INTERVAL_SEC,
                MAX_TADPOLE_SPAWN_COUNT,
                PrefabType.FireTadpole,
                PrefabType.LightningTadpole));
        gameObject.setElement(EnumSet.of(ElementType.FIRE, ElementType.LIGHTNING));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
