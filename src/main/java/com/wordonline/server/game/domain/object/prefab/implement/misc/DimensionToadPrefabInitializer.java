package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.LimitedSequenceSpawner;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.KeepDistanceMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("dimension_toad_prefab")
public class DimensionToadPrefabInitializer extends PrefabInitializer {

    private static final float TADPOLE_SPAWN_INTERVAL_SEC = 5f;
    private static final int INFINITE_TADPOLE_SPAWN_COUNT = 0;
    private static final float KEEP_DISTANCE_RANGE = 4f;

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
        gameObject.addComponent(new KeepDistanceMob(gameObject,
                (int) parameters.getValue("dimension_toad", "hp"),
                (float) parameters.getValue("dimension_toad", "speed"),
                TargetMask.ANY.bit,
                KEEP_DISTANCE_RANGE));
        gameObject.addComponent(new LimitedSequenceSpawner(
                gameObject,
                TADPOLE_SPAWN_INTERVAL_SEC,
                INFINITE_TADPOLE_SPAWN_COUNT,
                PrefabType.FireTadpole,
                PrefabType.LightningTadpole));
        gameObject.setElement(EnumSet.of(ElementType.FIRE, ElementType.LIGHTNING));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
