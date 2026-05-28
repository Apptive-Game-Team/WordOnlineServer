package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.BubbleSpiritMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("bubble_spirit_prefab")
public class BubbleSpiritPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public BubbleSpiritPrefabInitializer(Parameters parameters) {
        super(PrefabType.BubbleSpirit);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("bubble_spirit", "mass")));
        gameObject.addComponent(new ZPhysics(gameObject, GameConfig.AERIAL_MOB_INIT_HEIGHT));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("bubble_spirit", "radius"), false));
        gameObject.addComponent(new BubbleSpiritMob(gameObject,
                (int) parameters.getValue("bubble_spirit", "hp"),
                (float) parameters.getValue("bubble_spirit", "speed"),
                (int) parameters.getValue("bubble_spirit", "damage"),
                (float) parameters.getValue("bubble_spirit", "attack_interval"),
                (float) parameters.getValue("bubble_spirit", "attack_range"),
                (float) parameters.getValue("shoot", "speed")
        ));
        gameObject.setElement(EnumSet.of(ElementType.WATER, ElementType.WIND));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
