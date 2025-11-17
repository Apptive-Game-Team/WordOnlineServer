package com.wordonline.server.game.domain.object.prefab.implement.misc.third;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.ProjectileRangeAttackMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

@Component("cloud_dragon_prefab")
public class CloudDragonPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public CloudDragonPrefabInitializer(Parameters parameters) {
        super(PrefabType.CloudDragon);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("cloud_dragon", "mass")));
        gameObject.addComponent(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("cloud_dragon", "radius"), false));
        gameObject.addComponent(new ProjectileRangeAttackMob(gameObject,
                (int) parameters.getValue("cloud_dragon", "hp"),
                (float) parameters.getValue("cloud_dragon", "speed"),
                TargetMask.ANY.bit,
                (int) parameters.getValue("cloud_dragon", "damage"),
                (float) parameters.getValue("cloud_dragon", "attack_interval"),
                (float) parameters.getValue("cloud_dragon", "attack_range"),
                "WaterShot",
                0.5f
        ));
        gameObject.setElement(EnumSet.of(ElementType.WATER, ElementType.WIND));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));

        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("cloud_dragon", "attack_range"), true));
        gameObject.addComponent(new EffectProvider(gameObject, Effect.Wet));
    }
}
