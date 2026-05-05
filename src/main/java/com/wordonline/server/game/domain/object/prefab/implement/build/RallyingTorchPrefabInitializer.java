package com.wordonline.server.game.domain.object.prefab.implement.build;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.magic.RallyingTorch;
import com.wordonline.server.game.domain.object.component.mob.simple.DummyMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("rallying_torch_prefab")
public class RallyingTorchPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public RallyingTorchPrefabInitializer(Parameters parameters) {
        super(PrefabType.RallyingTorch);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("rallying_torch", "mass")));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("rallying_torch", "radius"), false));
        gameObject.addComponent(new DummyMob(gameObject, (int) parameters.getValue("rallying_torch", "hp")));
        gameObject.addComponent(new RallyingTorch(
                gameObject,
                (float) parameters.getValue("rallying_torch", "attack_interval"),
                (float) parameters.getValue("rallying_torch", "range"),
                (float) parameters.getValue("rallying_torch", "buff_duration"),
                (float) parameters.getValue("rallying_torch", "buff_amount")));
        gameObject.addComponent(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("rallying_torch", "duration")));
        gameObject.setElement(EnumSet.of(ElementType.FIRE, ElementType.WIND));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
