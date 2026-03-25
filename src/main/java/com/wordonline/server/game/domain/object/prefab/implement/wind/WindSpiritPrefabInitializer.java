package com.wordonline.server.game.domain.object.prefab.implement.wind;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.SelfDestructMob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.Slime;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component
public class WindSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public WindSpiritPrefabInitializer(
            Parameters parameters) {
        super(PrefabType.WindSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("wind_spirit", "mass")));
        gameObject.addComponent(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("wind_spirit", "radius"), false));
        gameObject.addComponent(new SelfDestructMob(gameObject,
                (int) parameters.getValue("wind_spirit", "hp"),
                (float) parameters.getValue("wind_spirit", "speed"),
                TargetMask.AIR.bit,
                (int) parameters.getValue("wind_spirit", "damage"),
                (float) parameters.getValue("wind_spirit", "attack_interval"),
                (float) parameters.getValue("wind_spirit", "attack_range")));
        gameObject.setElement(ElementType.WIND);
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
