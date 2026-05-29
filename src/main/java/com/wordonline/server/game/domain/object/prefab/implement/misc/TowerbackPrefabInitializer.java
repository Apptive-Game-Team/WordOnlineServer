package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.simple.Tower;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("towerback_prefab")
public class TowerbackPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public TowerbackPrefabInitializer(Parameters parameters) {
        super(PrefabType.Towerback);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("towerback", "mass")));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("towerback", "radius"), false));
        gameObject.addComponent(new Tower(gameObject,
                (int) parameters.getValue("towerback", "hp"),
                (int) parameters.getValue("towerback", "damage"),
                TargetMask.AIR.bit,
                (float) parameters.getValue("towerback", "attack_interval"),
                (float) parameters.getValue("towerback", "attack_range")
        ));
        gameObject.setElement(ElementType.ROCK);
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
